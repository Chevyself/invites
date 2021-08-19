package me.googas.invites.commands.providers;

import lombok.NonNull;
import me.googas.commands.bukkit.context.CommandContext;
import me.googas.commands.bukkit.providers.type.BukkitArgumentProvider;
import me.googas.commands.bukkit.providers.type.BukkitExtraArgumentProvider;
import me.googas.commands.exceptions.ArgumentProviderException;
import me.googas.invites.Invites;
import me.googas.invites.MembersSubloader;
import me.googas.invites.TeamMember;
import org.bukkit.entity.Player;

public class TeamMemberProvider implements BukkitExtraArgumentProvider<TeamMember> {
    @Override
    public @NonNull Class<TeamMember> getClazz() {
        return TeamMember.class;
    }

    @Override
    public @NonNull TeamMember getObject(@NonNull CommandContext context) throws ArgumentProviderException {
        if (context.getSender() instanceof Player) {
            return Invites.getLoader().getSubloader(MembersSubloader.class).getMember((Player) context.getSender());
        }
        throw new ArgumentProviderException(context.getMessagesProvider().playersOnly(context));
    }
}
