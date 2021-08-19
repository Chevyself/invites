package me.googas.invites;

import lombok.NonNull;
import me.googas.invites.sql.SqlTeam;
import me.googas.invites.sql.SqlTeamMember;
import me.googas.lazy.Subloader;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface MembersSubloader extends Subloader {

    @NonNull
    TeamMember getMember(@NonNull OfflinePlayer player);
}
