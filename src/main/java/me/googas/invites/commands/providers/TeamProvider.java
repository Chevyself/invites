package me.googas.invites.commands.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import me.googas.commands.bukkit.context.CommandContext;
import me.googas.commands.bukkit.providers.type.BukkitArgumentProvider;
import me.googas.commands.bukkit.providers.type.BukkitExtraArgumentProvider;
import me.googas.commands.bukkit.providers.type.BukkitMultiArgumentProvider;
import me.googas.commands.exceptions.ArgumentProviderException;
import me.googas.invites.Invites;
import me.googas.invites.Team;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamsSubloader;
import me.googas.starbox.Strings;

public class TeamProvider
    implements BukkitExtraArgumentProvider<Team>,
        BukkitMultiArgumentProvider<Team>,
        BukkitArgumentProvider<Team> {

  @Override
  public @NonNull Team fromString(@NonNull String string, @NonNull CommandContext context)
      throws ArgumentProviderException {
    Optional<? extends Team> optional =
        Invites.getLoader()
            .getSubloader(TeamsSubloader.class)
            .getTeam(context.get(string, int.class, context));
    if (optional.isPresent()) {
      return optional.get();
    }
    throw new ArgumentProviderException("&c" + string + " does not match a team id");
  }

  @Override
  public @NonNull List<String> getSuggestions(@NonNull String string, CommandContext context) {
    // TODO
    return new ArrayList<>();
  }

  @Override
  public @NonNull Team fromStrings(@NonNull String[] strings, @NonNull CommandContext context)
      throws ArgumentProviderException {
    String string = Strings.fromArray(strings);
    Optional<? extends Team> optional =
        Invites.getLoader().getSubloader(TeamsSubloader.class).getTeam(string);
    if (optional.isPresent()) {
      return optional.get();
    } else {
      throw new ArgumentProviderException("&c" + string + " did not match a team");
    }
  }

  @Override
  public @NonNull List<String> getSuggestions(@NonNull CommandContext commandContext) {
    // TODO
    return new ArrayList<>();
  }

  @Override
  public @NonNull Class<Team> getClazz() {
    return Team.class;
  }

  @Override
  public @NonNull Team getObject(@NonNull CommandContext context) throws ArgumentProviderException {
    TeamMember member = context.get(TeamMember.class, context);
    if (member.getTeam().isPresent()) {
      return member.getTeam().get();
    }
    throw new ArgumentProviderException("You are not in a team");
  }
}
