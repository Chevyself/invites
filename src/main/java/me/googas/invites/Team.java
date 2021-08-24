package me.googas.invites;

import lombok.NonNull;
import me.googas.net.cache.Catchable;

import java.util.Collection;

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
