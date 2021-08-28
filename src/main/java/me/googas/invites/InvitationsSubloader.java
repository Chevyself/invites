package me.googas.invites;

import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;
import me.googas.lazy.Subloader;

public interface InvitationsSubloader extends Subloader {

  Optional<? extends TeamInvitation> getInvitation(
      @NonNull TeamMember invited, @NonNull TeamMember leader, @NonNull InvitationStatus status);

  @NonNull
  TeamInvitation createInvitation(@NonNull TeamMember leader, @NonNull TeamMember member)
      throws TeamException;

  boolean cancelAll(@NonNull Team team);

  boolean cancelAll(@NonNull TeamMember leader);

  @NonNull
  Collection<? extends TeamInvitation> getInvitations(
      @NonNull TeamMember member, @NonNull InvitationStatus status);
}
