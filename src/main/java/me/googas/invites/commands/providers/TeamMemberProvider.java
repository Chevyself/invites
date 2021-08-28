package me.googas.invites.commands.providers;

import java.util.List;
import lombok.NonNull;
import me.googas.commands.bukkit.context.CommandContext;
import me.googas.commands.bukkit.providers.type.BukkitArgumentProvider;
import me.googas.commands.bukkit.providers.type.BukkitExtraArgumentProvider;
import me.googas.commands.exceptions.ArgumentProviderException;
import me.googas.invites.Invites;
import me.googas.invites.MembersSubloader;
import me.googas.invites.TeamMember;
import me.googas.starbox.utility.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TeamMemberProvider
    implements BukkitExtraArgumentProvider<TeamMember>, BukkitArgumentProvider<TeamMember> {
  @Override
  public @NonNull TeamMember fromString(@NonNull String string, @NonNull CommandContext context)
      throws ArgumentProviderException {
    return Invites.getLoader()
        .getSubloader(MembersSubloader.class)
        .getMember(context.get(string, OfflinePlayer.class, context));
  }

  @Override
  public @NonNull List<String> getSuggestions(
      @NonNull String string, CommandContext commandContext) {
    return Players.getOnlinePlayersNames();
  }

  @Override
  public @NonNull Class<TeamMember> getClazz() {
    return TeamMember.class;
  }

  @Override
  public @NonNull TeamMember getObject(@NonNull CommandContext context)
      throws ArgumentProviderException {
    if (context.getSender() instanceof Player) {
      return Invites.getLoader()
          .getSubloader(MembersSubloader.class)
          .getMember((Player) context.getSender());
    }
    throw new ArgumentProviderException(context.getMessagesProvider().playersOnly(context));
  }
}
