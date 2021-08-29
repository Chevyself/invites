package me.googas.invites.events.teams;

import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.googas.invites.Team;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.invites.events.InvitesCancellable;
import me.googas.invites.events.InvitesEvent;

public class AsyncSwitchTeamEvent extends InvitesEvent implements InvitesCancellable {

  @NonNull @Getter private final TeamMember member;
  private final Team team;
  private final TeamRole role;
  @Getter @Setter private boolean cancelled;

  public AsyncSwitchTeamEvent(@NonNull TeamMember member, Team team, TeamRole role) {
    this.member = member;
    this.team = team;
    this.role = role;
  }

  @NonNull
  public Optional<Team> getTeam() {
    return Optional.ofNullable(this.team);
  }

  @NonNull
  public Optional<TeamRole> getRole() {
    return Optional.ofNullable(this.role);
  }
}
