package me.googas.invites;

import lombok.NonNull;
import me.googas.net.cache.Catchable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface TeamMember extends Catchable {

    boolean setTeam(@NonNull Team team, @NonNull TeamRole role);

    @NonNull
    default Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(this.getUniqueId()));
    }

    @NonNull
    UUID getUniqueId();

    @NonNull
    Optional<? extends Team> getTeam();

    @NonNull
    Optional<TeamRole> getRole();
}
