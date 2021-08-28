package me.googas.invites.commands.providers;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import me.googas.commands.bukkit.context.CommandContext;
import me.googas.commands.bukkit.providers.type.BukkitArgumentProvider;
import me.googas.commands.exceptions.ArgumentProviderException;
import me.googas.invites.InvitationStatus;
import me.googas.invites.InvitationsSubloader;
import me.googas.invites.Invites;
import me.googas.invites.MembersSubloader;
import me.googas.invites.TeamInvitation;
import me.googas.invites.TeamMember;
import me.googas.starbox.utility.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TeamInvitationProvider implements BukkitArgumentProvider<TeamInvitation> {

  @Override
  public @NonNull Class<TeamInvitation> getClazz() {
    return TeamInvitation.class;
  }

  @Override
  public @NonNull TeamInvitation fromString(@NonNull String string, @NonNull CommandContext context)
      throws ArgumentProviderException {
    if (context.getSender() instanceof Player) {
      TeamMember invited =
          Invites.getLoader()
              .getSubloader(MembersSubloader.class)
              .getMember((OfflinePlayer) context.getSender());
      TeamMember leader = context.get(string, TeamMember.class, context);
      Optional<? extends TeamInvitation> optional =
          Invites.getLoader()
              .getSubloader(InvitationsSubloader.class)
              .getInvitation(invited, leader, InvitationStatus.WAITING);
      if (optional.isPresent()) {
        return optional.get();
      } else {
        throw new ArgumentProviderException("You dont have an invitation from " + string);
      }
    }
    throw new ArgumentProviderException(context.getMessagesProvider().playersOnly(context));
  }

  @Override
  public @NonNull List<String> getSuggestions(@NonNull String string, CommandContext context) {
    return Players.getOnlinePlayersNames();
  }
}
