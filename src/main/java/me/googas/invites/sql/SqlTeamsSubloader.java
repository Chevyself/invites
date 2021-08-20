package me.googas.invites.sql;

import lombok.NonNull;
import me.googas.invites.Invites;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.invites.Team;
import me.googas.invites.TeamException;
import me.googas.invites.TeamsSubloader;
import me.googas.lazy.sql.LazySQL;
import me.googas.lazy.sql.LazySQLSubloader;
import me.googas.lazy.sql.LazySQLSubloaderBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class SqlTeamsSubloader extends LazySQLSubloader implements TeamsSubloader {

    @NonNull
    private final LazySchema schema;

    /**
     * Start the subloader.
     *
     * @param parent the sql parent
     * @param schema
     */
    protected SqlTeamsSubloader(@NonNull LazySQL parent, @NonNull LazySchema schema) {
        super(parent);
        this.schema = schema;
    }

    public boolean disband(@NonNull SqlTeam team) {
        try {
            PreparedStatement statement = this.formatStatement("DELETE FROM `teams` WHERE `id`={0};", team.getId());
            statement.executeUpdate();
            this.parent.getCache().remove(team);
            return true;
        } catch (SQLException e) {
            Invites.handle(e, () -> "Could not delete team: " + team.getId());
        }
        return false;
    }

    @Override
    public @NonNull SqlTeamsSubloader createTable() throws SQLException {
        this.statementOf(this.schema.getSql("teams.create-table")).execute();
        return this;
    }

    @Override
    public @NonNull Team createTeam(@NonNull String name, @NonNull TeamMember leader) throws TeamException {
        try {
            SqlTeam team = new SqlTeam(0, name);
            PreparedStatement statement = this.statementOf("INSERT INTO `teams` (`name`) VALUES('{0}');", Statement.RETURN_GENERATED_KEYS, name);
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                if (this.schema.getType() == LazySchema.Type.SQL) {
                    team.setId(resultSet.getInt("id"));
                } else {
                    ResultSet keysResult = this.statementOf("SELECT last_insert_rowid()").executeQuery();
                    if (keysResult.next()) {
                        team.setId(resultSet.getInt(1));
                    }
                }
            }
            leader.setTeam(team, TeamRole.LEADER);
            this.parent.getCache().add(team);
            return team;
        } catch (SQLException e) {
            throw new TeamException("Team could not be created due to a SQLException", e);
        }
    }

    @Override
    public @NonNull Optional<SqlTeam> getTeam(int id) {
        return Optional.ofNullable(this.parent.getCache().get(SqlTeam.class, team -> team.getId() == id).orElseGet(() -> {
            try {
                ResultSet resultSet = this.formatStatement("SELECT * FROM `teams` WHERE `id`={0} LIMIT 1;", id).executeQuery();
                if (resultSet.next()) {
                    SqlTeam team = SqlTeam.of(resultSet);
                    this.parent.getCache().add(team);
                    return team;
                }
            } catch (SQLException e) {
                Invites.handle(e, () -> "Could not get team for: " + id);
            }
            return null;
        }));
    }

    @Override
    public @NonNull Optional<SqlTeam> getTeam(@NonNull String name) {
        return Optional.ofNullable(this.parent.getCache().get(SqlTeam.class, team -> team.getName().equalsIgnoreCase(name)).orElseGet(() -> {
            try {
                ResultSet resultSet = this.formatStatement("SELECT DISTINCT * FROM `teams` WHERE LOWER(`name`) LIKE LOWER('{0}' LIMIT 1);", name).executeQuery();
                if (resultSet.next()) {
                    SqlTeam team = SqlTeam.of(resultSet);
                    this.parent.getCache().add(team);
                    return team;
                }
            } catch (SQLException e) {
                Invites.handle(e, () -> "Could not get team by the name: " + name);
            }
            return null;
        }));
    }

    public static class Builder implements LazySQLSubloaderBuilder {

        @NonNull
        private final LazySchema schema;

        public Builder(@NonNull LazySchema schema) {
            this.schema = schema;
        }

        @Override
        @NonNull
        public SqlTeamsSubloader build(@NonNull LazySQL parent) {
            return new SqlTeamsSubloader(parent, this.schema);
        }
    }
}
