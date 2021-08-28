package me.googas.invites;

import java.util.Collection;
import lombok.NonNull;
import me.googas.net.cache.Catchable;

public interface Team extends Catchable {

  boolean disband();

  boolean rename(@NonNull String name);

  @NonNull
  Collection<? extends TeamMember> getMembers(@NonNull TeamRole... roles);

  int getId();

  @NonNull
  String getName();

  @NonNull
  Collection<? extends TeamMember> getMembers();
}
