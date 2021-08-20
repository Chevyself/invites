package me.googas.invites.sql;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.googas.invites.Invites;
import me.googas.invites.TeamRole;
import me.googas.invites.Team;
import me.googas.invites.TeamMember;
import me.googas.lazy.sql.SQLElement;
import me.googas.starbox.time.Time;
import me.googas.starbox.time.unit.Unit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

public class SqlTeamMember implements TeamMember {

    @NonNull @Getter
    private final UUID uniqueId;
    private int teamId;
    private TeamRole role;

    public SqlTeamMember(@NonNull UUID uniqueId, int teamId, TeamRole role) {
        this.uniqueId = uniqueId;
        this.teamId = teamId;
        this.role = role;
    }

    public boolean leaveTeam() {
        return this.setTeam(null, null);
    }

    @Override
    public @NonNull Time getToRemove() {
        return Time.of(5, Unit.MINUTES);
    }

    @Override
    public boolean setTeam(Team team, TeamRole role) {
        if (Invites.getLoader().getSubloader(SqlMembersSubloader.class).setTeam(this, team, role)) {
            this.teamId = team == null ? -1 : team.getId();
            this.role = role;
            return true;
        }
        return false;
    }

    @NonNull
    public static SqlTeamMember of(@NonNull ResultSet resultSet) throws SQLException {
        return new SqlTeamMember(UUID.fromString(resultSet.getString("uuid")), resultSet.getInt("team"), TeamRole.valueOf(resultSet.getString("role")));
    }

    @Override
    public @NonNull Optional<SqlTeam> getTeam() {
        return Invites.getLoader().getSubloader(SqlTeamsSubloader.class).getTeam(this.teamId);
    }

    @Override
    public @NonNull Optional<TeamRole> getRole() {
        return Optional.ofNullable(this.role);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SqlTeamMember.class.getSimpleName() + "[", "]")
                .add("uniqueId=" + this.uniqueId)
                .add("teamId=" + this.teamId)
                .add("role=" + this.role)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SqlTeamMember)) return false;
        SqlTeamMember that = (SqlTeamMember) o;
        return this.uniqueId.equals(that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uniqueId);
    }
}
