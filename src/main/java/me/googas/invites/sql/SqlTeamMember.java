package me.googas.invites.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import lombok.NonNull;
import me.googas.invites.Invites;
import me.googas.invites.Team;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.invites.events.teams.AsyncSwitchTeamEvent;
import me.googas.starbox.modules.channels.PlayerChannel;
import me.googas.starbox.time.Time;
import me.googas.starbox.time.unit.Unit;

public class SqlTeamMember extends PlayerChannel implements TeamMember {

  private int teamId;
  private TeamRole role;

  public SqlTeamMember(@NonNull UUID uniqueId, int teamId, TeamRole role) {
    super(uniqueId);
    this.teamId = teamId;
    this.role = role;
  }

  @NonNull
  public static SqlTeamMember of(@NonNull ResultSet resultSet) throws SQLException {
    String role = resultSet.getString("role");
    return new SqlTeamMember(
        UUID.fromString(resultSet.getString("uuid")),
        resultSet.getInt("team"),
        role == null ? null : TeamRole.valueOf(role));
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
    if (new AsyncSwitchTeamEvent(this, team, role).notCancelled()
        && Invites.getLoader().getSubloader(SqlMembersSubloader.class).setTeam(this, team, role)) {
      this.teamId = team == null ? -1 : team.getId();
      this.role = role;
      return true;
    }
    return false;
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
        .add("uniqueId=" + this.getUniqueId())
        .add("teamId=" + this.teamId)
        .add("role=" + this.role)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SqlTeamMember)) return false;
    SqlTeamMember that = (SqlTeamMember) o;
    return this.getUniqueId().equals(that.getUniqueId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getUniqueId());
  }
}
