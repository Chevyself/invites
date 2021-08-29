package me.googas.invites.events.teams;

import lombok.NonNull;
import me.googas.invites.Team;

public class AsyncTeamCreatedEvent extends AbstractTeamEvent {
  public AsyncTeamCreatedEvent(@NonNull Team team) {
    super(team);
  }
}
