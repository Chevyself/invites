package me.googas.invites;

import lombok.NonNull;
import me.googas.net.cache.Catchable;

import java.util.Collection;

public interface Team extends Catchable {

    boolean disband();

    boolean rename(@NonNull String name);

    int getId();

    @NonNull
    String getName();

    @NonNull
    Collection<? extends TeamMember> getMembers();
}
