package me.googas.invites.events.teams;

import lombok.Getter;
import lombok.NonNull;
import me.googas.invites.Team;
import me.googas.invites.events.InvitesEvent;

public class AbstractTeamEvent extends InvitesEvent implements TeamEvent {

  @NonNull @Getter private final Team team;

  public AbstractTeamEvent(@NonNull Team team) {
    this.team = team;
  }
}
