package me.googas.invites.commands.providers;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamProvider implements BukkitExtraArgumentProvider<Team>, BukkitMultiArgumentProvider<Team> {

    @Override
    public @NonNull Team fromStrings(@NonNull String[] strings, @NonNull CommandContext context) throws ArgumentProviderException {
        String string = Strings.fromArray(strings);
        Optional<? extends Team> optional = Invites.getLoader().getSubloader(TeamsSubloader.class).getTeam(string);
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
