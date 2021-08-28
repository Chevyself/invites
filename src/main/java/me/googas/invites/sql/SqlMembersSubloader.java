package me.googas.invites.sql;

import lombok.NonNull;
import me.googas.invites.Invites;
import me.googas.invites.MembersSubloader;
import me.googas.invites.Team;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.lazy.sql.LazySQL;
import me.googas.lazy.sql.LazySQLSubloader;
import me.googas.lazy.sql.LazySQLSubloaderBuilder;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class SqlMembersSubloader extends LazySQLSubloader implements MembersSubloader {

    /**
     * Start the subloader.
     *
     * @param parent the sql parent
     */
    protected SqlMembersSubloader(@NonNull LazySQL parent) {
        super(parent);
    }

    public @NonNull List<SqlTeamMember> getMembers(@NonNull Team team) {
        try {
            return this.getMembers(this.formatStatement("SELECT * FROM `members` WHERE `team`={0};", team.getId()).executeQuery());
        } catch (SQLException e) {
            Invites.handle(e, () -> "There's been an error while trying to get a members for team: " + team.getId());
        }
        return new ArrayList<>();
    }

    @NonNull
    public SqlTeamMember getMember(@NonNull OfflinePlayer player) {
        SqlTeamMember sqlMember = this.parent.getCache().get(SqlTeamMember.class, catchable -> catchable.getUniqueId().equals(player.getUniqueId()), true).orElseGet(() -> {
            SqlTeamMember member = null;
            try {
                PreparedStatement statement = this.formatStatement("SELECT * FROM `members` WHERE `uuid`='{0}' LIMIT 1;", player.getUniqueId());
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    member = SqlTeamMember.of(resultSet);
                } else {
                    member = new SqlTeamMember(player.getUniqueId(), 0, null);
                    this.formatStatement("INSERT INTO `members`(`uuid`) VALUES('{0}');", player.getUniqueId()).execute();
                }
                this.parent.getCache().add(member);
            } catch (SQLException e) {
                Invites.handle(e, () -> "There's been an error while trying to get a member for: " + player.getUniqueId());
            }
            return member;
        });
        return Objects.requireNonNull(sqlMember, "There seems to been an error while trying to get a member for: " + player.getUniqueId());
    }

    @NonNull
    private List<SqlTeamMember> getMembers(@NonNull ResultSet resultSet) throws SQLException {
        List<SqlTeamMember> loaded = new ArrayList<>();
        while (resultSet.next()) {
            loaded.add(SqlTeamMember.of(resultSet));
        }
        Set<UUID> matched = new HashSet<>();
        loaded.removeIf(member -> {
            if (this.parent.getCache().contains(member)) {
                matched.add(member.getUniqueId());
                return true;
            }
            return false;
        });
        loaded.forEach(member -> this.parent.getCache().add(member));
        loaded.addAll(this.parent.getCache().getMany(SqlTeamMember.class, member -> matched.contains(member.getUniqueId())));
        return loaded;
    }

    @Override
    public @NonNull List<SqlTeamMember> getLeaders() {
        try {
            return this.getMembers(this.formatStatement("SELECT * FROM `members` WHERE `role`='{0}';", TeamRole.LEADER).executeQuery());
        } catch (SQLException e) {
            Invites.handle(e, () -> "There's been an error while trying to get leaders");
        }
        return new ArrayList<>();
    }

    public boolean setTeam(@NonNull SqlTeamMember member, Team team, TeamRole role) {
        try {
            PreparedStatement statement = this.formatStatement("UPDATE `members` SET `team`=?, `role`=? WHERE `uuid`='{0}';", member.getUniqueId());
            if (team == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, team.getId());
            }
            if (role == null) {
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(2, role.toString());
            }
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            Invites.handle(e, () -> "Could not set team for: " + member.getUniqueId());
            return false;
        }
    }

    @Override
    public @NonNull SqlMembersSubloader createTable() throws SQLException {
        this.statementOf("CREATE TABLE IF NOT EXISTS `members` (" +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`team` INT DEFAULT NULL," +
                "`role` VARCHAR(50) DEFAULT 'NORMAL'," +
                "PRIMARY KEY (`uuid`));").execute();
        return this;
    }

    public static class Builder implements LazySQLSubloaderBuilder {

        @Override @NonNull
        public SqlMembersSubloader build(@NonNull LazySQL parent) {
            return new SqlMembersSubloader(parent);
        }
    }
}
