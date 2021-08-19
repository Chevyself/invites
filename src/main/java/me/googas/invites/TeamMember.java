package me.googas.invites;

import lombok.NonNull;
import me.googas.invites.sql.SqlTeam;
import me.googas.net.cache.Catchable;

import java.util.Optional;
import java.util.UUID;

public interface TeamMember extends Catchable {

    boolean setTeam(@NonNull Team team, @NonNull TeamRole role);

    @NonNull
    UUID getUniqueId();

    @NonNull
    Optional<? extends Team> getTeam();

    @NonNull
    Optional<TeamRole> getRole();
}
