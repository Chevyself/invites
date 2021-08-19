package me.googas.invites.commands;

import lombok.Getter;
import lombok.NonNull;
import me.googas.commands.annotations.Free;
import me.googas.commands.annotations.Required;
import me.googas.commands.bukkit.CommandManager;
import me.googas.commands.bukkit.StarboxBukkitCommand;
import me.googas.commands.bukkit.annotations.Command;
import me.googas.commands.bukkit.context.CommandContext;
import me.googas.commands.bukkit.result.Result;
import me.googas.invites.Invites;
import me.googas.invites.MembersSubloader;
import me.googas.invites.Team;
import me.googas.invites.TeamException;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.invites.TeamsSubloader;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TeamCommand extends StarboxBukkitCommand {

    @NonNull @Getter
    private final List<StarboxBukkitCommand> children;

    public TeamCommand(@NonNull CommandManager manager) {
        super("teams", false, manager);
        this.children = new ArrayList<>(manager.parseCommands(new SubCommands()));
    }

    @Override
    public Result execute(@NonNull CommandContext context) {
        // TODO show ui with commands
        return new Result();
    }

    @Override
    public boolean hasAlias(@NonNull String alias) {
        return alias.equalsIgnoreCase("teams");
    }

    public static class SubCommands {
        @Command(aliases = "create", description = "Create a team", permission = "invites.teams.create", async = true)
        public Result create(TeamMember member, @Required(name = "name", description = "The name of the team") String name) throws TeamException {
            TeamsSubloader subloader = Invites.getLoader().getSubloader(TeamsSubloader.class);
            if (member.getTeam().isPresent()) {
                return new Result("&cYou already have a team");
            } else {
                Team team = subloader.createTeam(name, member);
                return new Result("&7Your team: &6{0} &7has been created", team.getName());
            }
        }

        @Command(aliases = "list", description = "List your team", permission = "invites.teams.list", async = true)
        public Result list(Team team) {
            // TODO make it more beautiful
            StringBuilder builder = new StringBuilder();
            team.getMembers().forEach(member -> {
                builder.append("- ").append(member.getUniqueId()).append(" role: ").append(member.getRole().orElse(TeamRole.NORMAL));
            });
            return new Result(builder.toString());
        }

        @Command(aliases = "disband", description = "Disband a team", permission = "invites.teams.disband", async = true)
        public Result disband(TeamMember member, Team team) {
            boolean isLeader = member.getRole().isPresent() && member.getRole().get() == TeamRole.LEADER;
            if (isLeader && team.disband()) {
                return new Result("&7Your team has been disbanded");
            } else if (!isLeader) {
                return new Result("&cYou are not the leader of your team");
            } else {
                return new Result("&cYour team could not be disbanded");
            }
        }
    }
}
