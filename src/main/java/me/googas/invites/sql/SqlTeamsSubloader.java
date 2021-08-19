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

    /**
     * Start the subloader.
     *
     * @param parent the sql parent
     */
    protected SqlTeamsSubloader(@NonNull LazySQL parent) {
        super(parent);
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
        this.statementOf("CREATE TABLE IF NOT EXISTS `teams` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(16) NOT NULL," +
                "PRIMARY KEY (`id`,`name`));").execute();
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
                team.setId(resultSet.getInt("id"));
            }
            leader.setTeam(team, TeamRole.LEADER);
            this.parent.getCache().add(team);
            return team;
        } catch (SQLException e) {
            throw new TeamException("Team could not be created due to a SQLException", e);
        }
    }

    @Override
    public @NonNull Optional<SqlTeam>  getTeam(int id) {
        return Optional.ofNullable(this.parent.getCache().get(SqlTeam.class, team -> team.getId() == id).orElseGet(() -> {
            try {
                ResultSet resultSet = this.formatStatement("SELECT * FROM `teams` WHERE `id`={0} LIMIT 1;", id).executeQuery();
                if (resultSet.next()) {
                    SqlTeam team = new SqlTeam(resultSet.getInt("id"), resultSet.getString("name"));
                    this.parent.getCache().add(team);
                    return team;
                }
            } catch (SQLException e) {
                Invites.handle(e, () -> "Could not get team for: " + id);
            }
            return null;
        }));
    }

    public static class Builder implements LazySQLSubloaderBuilder {

        @Override
        @NonNull
        public SqlTeamsSubloader build(@NonNull LazySQL parent) {
            return new SqlTeamsSubloader(parent);
        }
    }
}
