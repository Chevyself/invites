package me.googas.invites;

import lombok.NonNull;
import me.googas.net.cache.Catchable;
import me.googas.starbox.modules.channels.Channel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface TeamMember extends Catchable, Channel {

    boolean setTeam(@NonNull Team team, @NonNull TeamRole role);

    boolean leaveTeam();

    @NonNull
    default OfflinePlayer getOffline() {
        return Bukkit.getOfflinePlayer(this.getUniqueId());
    }

    @NonNull
    default Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(this.getUniqueId()));
    }

    @NonNull
    default String getName() {
        String name = this.getOffline().getName();
        return name == null ? this.getUniqueId().toString() : name;
    }

    @NonNull
    UUID getUniqueId();

    @NonNull
    Optional<? extends Team> getTeam();

    @NonNull
    Optional<TeamRole> getRole();
}
