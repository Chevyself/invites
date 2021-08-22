package me.googas.invites;

import lombok.NonNull;
import me.googas.lazy.Subloader;
import org.bukkit.OfflinePlayer;

public interface MembersSubloader extends Subloader {

    @NonNull
    TeamMember getMember(@NonNull OfflinePlayer player);
}
