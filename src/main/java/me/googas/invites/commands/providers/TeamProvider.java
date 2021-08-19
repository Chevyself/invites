package me.googas.invites.commands.providers;

import lombok.NonNull;
import me.googas.commands.bukkit.context.CommandContext;
import me.googas.commands.bukkit.providers.type.BukkitExtraArgumentProvider;
import me.googas.commands.exceptions.ArgumentProviderException;
import me.googas.invites.Team;
import me.googas.invites.TeamMember;

public class TeamProvider implements BukkitExtraArgumentProvider<Team> {
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
