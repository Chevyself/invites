package me.googas.invites.events.teams;

import lombok.NonNull;
import me.googas.invites.Team;

public interface TeamEvent {
  @NonNull
  Team getTeam();
}
