package me.googas.invites;

import lombok.NonNull;
import me.googas.invites.TeamMember;

public interface TeamInvitation {

    boolean accept();

    boolean deny();

    int getId();

    @NonNull
    TeamMember getLeader();

    @NonNull
    TeamMember getInvited();

    @NonNull
    InvitationStatus getStatus();
}
