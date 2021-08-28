package me.googas.invites.events;

import lombok.NonNull;
import me.googas.invites.Team;

public class TeamEvent extends InvitesEvent {

    @NonNull
    private final Team team;

    public TeamEvent(@NonNull Team team) {
        this.team = team;
    }
}
