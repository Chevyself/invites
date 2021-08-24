package me.googas.invites.commands;

import lombok.NonNull;
import me.googas.commands.annotations.Multiple;
import me.googas.commands.annotations.Parent;
import me.googas.commands.annotations.Required;
import me.googas.commands.bukkit.CommandManager;
import me.googas.commands.bukkit.annotations.Command;
import me.googas.commands.bukkit.context.CommandContext;
import me.googas.commands.bukkit.result.Result;
import me.googas.commands.bukkit.utils.BukkitUtils;
import me.googas.invites.Invites;
import me.googas.invites.MembersSubloader;
import me.googas.invites.Team;
import me.googas.invites.TeamException;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.invites.TeamsSubloader;
import me.googas.invites.ui.CommandButtonListener;
import me.googas.starbox.BukkitLine;
import me.googas.starbox.commands.StarboxParentCommand;
import me.googas.starbox.modules.ui.Button;
import me.googas.starbox.modules.ui.types.CustomInventory;
import me.googas.starbox.modules.ui.types.PaginatedInventory;
import me.googas.starbox.utility.items.ItemBuilder;
import me.googas.starbox.utility.items.meta.SkullMetaBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TeamsCommand {

    @Command(aliases = "create", description = "Create a team", permission = "invites.teams.create", async = true)
    public Result create(TeamMember member, @Multiple @Required(name = "name", description = "The name of the team") String name) throws TeamException {
        TeamsSubloader subloader = Invites.getLoader().getSubloader(TeamsSubloader.class);
        if (member.getTeam().isPresent()) {
            return BukkitLine.localized(member, "invitations.create.already").asResult();
        } else {
            if (subloader.getTeam(name).isPresent()) {
                return BukkitLine.localized(member, "invitations.create.already-name").format(name).asResult();
            } else {
                Team team = subloader.createTeam(name, member);
                return BukkitLine.localized(member, "invitations.create.created").format(name).asResult();
            }
        }
    }

    @Command(aliases = "kick", description = "Kick a player from your team", permission = "invites.teams.kick", async = true)
    public Result kick(TeamMember leader, @Required(name = "member", description = "The member to kick") TeamMember member) {
        Optional<? extends Team> optionalTeam = leader.getTeam();
        Optional<? extends Team> memberTeam = member.getTeam();
        if (optionalTeam.isPresent() && leader.getRole().isPresent() && (optionalTeam.get().equals(memberTeam.orElse(null)))) {
            TeamRole role = leader.getRole().get();
            // TODO allow subleaders to kick this should be implemented when promote is also included
            if (role == TeamRole.LEADER) {
                if (member.leaveTeam()) {
                    return BukkitLine.localized(member, "invitations.kick.kicked").format(member.getName()).asResult();
                } else {
                    return BukkitLine.localized(member, "invitations.kick.not").format(member.getName()).asResult();
                }
            } else {
                return BukkitLine.localized(member, "invitations.invite.not-leader").asResult();
            }
        } else if (optionalTeam.isPresent() && !optionalTeam.get().equals(memberTeam.orElse(null))) {
            return BukkitLine.localized(member, "invitations.kick.not-same").asResult();
        } else {
            return BukkitLine.localized(member, "invitations.invite.no-team").asResult();
        }
    }

    @Command(aliases = "leave", description = "Leave your team", permission = "invites.teams.leave", async = true)
    public Result leave(TeamMember member) {
        if (member.getTeam().isPresent()) {
            if (member.getRole().isPresent() && member.getRole().get() != TeamRole.LEADER) {
                if (member.leaveTeam()) {
                    return BukkitLine.localized(member, "invitations.leave.left").asResult();
                } else {
                    return BukkitLine.localized(member, "invitations.leave.not").asResult();
                }
            } else {
                return BukkitLine.localized(member, "invitations.leave.leader").asResult();
            }
        } else {
            return BukkitLine.localized(member, "invitations.invite.no-team").asResult();
        }
    }

    @Command(aliases = "rename", description = "Rename your team", permission = "invites.teams.rename", async = true)
    public Result rename(TeamMember member, @Multiple @Required(name = "name", description = "The new name of the team") String name) {
        Optional<? extends Team> team = member.getTeam();
        Optional<TeamRole> role = member.getRole();
        Optional<? extends Team> optionalTeam = Invites.getLoader().getSubloader(TeamsSubloader.class).getTeam(name);
        if (team.isPresent() && !optionalTeam.isPresent() && (role.isPresent() && (role.get() == TeamRole.LEADER || role.get() == TeamRole.SUBLEADER))) {
            if (team.get().rename(name)) {
                return BukkitLine.localized(member, "invitations.rename.renamed").format(name).asResult();
            } else {
                return BukkitLine.localized(member, "invitations.rename.not").asResult();
            }
        } else if (team.isPresent() && optionalTeam.isPresent()) {
            return BukkitLine.localized(member, "invitations.create.already-name").format(name).asResult();
        } else if (team.isPresent() && (!role.isPresent() || role.get() == TeamRole.NORMAL)) {
            return BukkitLine.localized(member, "invitations.invite.not-leader").asResult();
        } else {
            return BukkitLine.localized(member, "invitations.invite.no-team").asResult();
        }
    }

    @Command(aliases = "disband", description = "Disband a team", permission = "invites.teams.disband", async = true)
    public Result disband(TeamMember member, Team team) {
        boolean isLeader = member.getRole().isPresent() && member.getRole().get() == TeamRole.LEADER;
        if (isLeader && team.disband()) {
            return BukkitLine.localized(member, "invitations.disband.done").asResult();
        } else if (!isLeader) {
            return BukkitLine.localized(member, "invitations.invite.not-leader").asResult();
        } else {
            return BukkitLine.localized(member, "invitations.disband.not").asResult();
        }
    }

    @Command(aliases = "view", description = "View a team", permission = "invites.teams.view", async = true)
    public Result view(Player viewer, @Required(name = "team", description = "The team to view") @Multiple Team team) {
        // TODO send a list to console sender
        viewer.closeInventory();
        viewer.openInventory(UI.team(viewer, team).getInventory());
        return new Result();
    }

    public static class Parent extends StarboxParentCommand {


        public Parent(@NonNull CommandManager manager) {
            super("teams", "Opens a UI to view and manage teams", "<teams>", Arrays.asList("team", "t"), false, manager);
        }

        @Override
        public Result execute(@NonNull CommandContext context) {
            CommandSender sender = context.getSender();
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.closeInventory();
                player.openInventory(UI.teams(player).getInventory());
                return new Result();
            }
            return Result.of(context.getMessagesProvider().playersOnly(context));
        }

        @Override
        public String getPermission() {
            return "invites.teams";
        }
    }

    public static class UI {

        @NonNull
        public static PaginatedInventory teams(@NonNull Player viewer) {
            // Keep viewer for future
            PaginatedInventory inventory = new PaginatedInventory(CustomInventory.EXTRA_LARGE, BukkitUtils.format("&9&lTeams &r&6%page%/&7%max%")).addDefaultToolbar();
            List<? extends TeamMember> leaders = Invites.getLoader().getSubloader(MembersSubloader.class).getLeaders();
            if (leaders.isEmpty()) {
                // TODO
            } else {
                leaders.forEach(leader -> {
                    Team team = leader.getTeam().orElseThrow(NullPointerException::new);
                    inventory.add(
                            Button.builder(UI.builder(leader.getOffline())
                                    .withMeta(meta -> meta.setName(team.getName()))
                                    .setAmount(team.getMembers().size()))
                                    .listen(new CommandButtonListener("teams view " + team.getName()))
                                    .build());
                });
            }
            return inventory;
        }

        @NonNull
        public static PaginatedInventory team(@NonNull Player viewer, @NonNull Team team) {
            PaginatedInventory inventory = new PaginatedInventory(team.getName()).addDefaultToolbar();
            team.getMembers().forEach(member -> {
                inventory.add(
                        Button.builder(UI.builder(member.getOffline()).withMeta(meta -> {
                            meta.setName("&6" + member.getName());
                            meta.setLore(BukkitUtils.format("&7Role &b{0}", member.getRole().orElse(TeamRole.NORMAL).toString().toLowerCase()));
                        })).build());
            });
            return inventory;
        }

        @NonNull
        public static ItemBuilder builder(@NonNull OfflinePlayer player) {
            return new ItemBuilder(Material.SKULL_ITEM).withMeta(meta -> {
                if (meta instanceof SkullMetaBuilder) {
                    ((SkullMetaBuilder) meta).setOwner(player);
                }
            });
        }
    }
}
