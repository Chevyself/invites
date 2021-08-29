package me.googas.invites.events.teams;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.googas.invites.TeamMember;
import me.googas.invites.events.InvitesCancellable;

public class AsyncTeamPreCreationEvent implements InvitesCancellable {

  @NonNull @Getter private final TeamMember user;
  @NonNull @Getter private final String name;
  @Getter @Setter private boolean cancelled;

  public AsyncTeamPreCreationEvent(@NonNull TeamMember user, @NonNull String name) {
    this.user = user;
    this.name = name;
  }
}
