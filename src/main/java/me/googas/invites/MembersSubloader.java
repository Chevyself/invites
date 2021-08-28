package me.googas.invites;

import java.util.List;
import lombok.NonNull;
import me.googas.lazy.Subloader;
import org.bukkit.OfflinePlayer;

public interface MembersSubloader extends Subloader {

  @NonNull
  TeamMember getMember(@NonNull OfflinePlayer player);

  @NonNull
  List<? extends TeamMember> getLeaders();
}
