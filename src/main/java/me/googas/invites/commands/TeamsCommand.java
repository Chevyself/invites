package me.googas.invites.commands;

import java.util.Arrays;
import java.util.Optional;
import lombok.NonNull;
import me.googas.commands.annotations.Multiple;
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
import me.googas.starbox.BukkitLine;
import me.googas.starbox.commands.StarboxParentCommand;
import me.googas.starbox.modules.ui.Button;
import me.googas.starbox.modules.ui.ButtonBuilder;
import me.googas.starbox.modules.ui.buttons.CommandButtonListener;
import me.googas.starbox.modules.ui.types.CustomInventory;
import me.googas.starbox.modules.ui.types.PaginatedInventory;
import me.googas.starbox.utility.Materials;
import me.googas.starbox.utility.items.ItemBuilder;
import me.googas.starbox.utility.items.meta.SkullMetaBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamsCommand {

  @Command(
      aliases = "create",
      description = "Create a team",
      permission = "invites.teams.create",
      async = true)
  public Result create(
      TeamMember member,
      @Multiple @Required(name = "name", description = "The name of the team") String name)
      throws TeamException {
    if (name.length() > 16)
      return BukkitLine.localized(member, "invitations.create.long-name").formatSample().asResult();
    TeamsSubloader subloader = Invites.getLoader().getSubloader(TeamsSubloader.class);
    if (member.getTeam().isPresent()) {
      return BukkitLine.localized(member, "invitations.create.already").formatSample().asResult();
    } else {
      if (subloader.getTeam(name).isPresent()) {
        return BukkitLine.localized(member, "invitations.create.already-name")
            .format(name)
            .formatSample()
            .asResult();
      } else {
        if (subloader.createTeam(name, member).isPresent()) {
          return BukkitLine.localized(member, "invitations.create.created")
              .format(name)
              .formatSample()
              .asResult();
        }
        // If team is not present the event might have been cancelled and a message must've been
        // sent
        return new Result();
      }
    }
  }

  @Command(
      aliases = "kick",
      description = "Kick a player from your team",
      permission = "invites.teams.kick",
      async = true)
  public Result kick(
      TeamMember leader,
      @Required(name = "member", description = "The member to kick") TeamMember member) {
    Optional<? extends Team> optionalTeam = leader.getTeam();
    Optional<? extends Team> memberTeam = member.getTeam();
    if (optionalTeam.isPresent()
        && leader.getRole().isPresent()
        && (optionalTeam.get().equals(memberTeam.orElse(null)))) {
      TeamRole role = leader.getRole().get();
      // TODO allow subleaders to kick this should be implemented when promote is also included
      if (role == TeamRole.LEADER) {
        if (member.leaveTeam()) {
          return BukkitLine.localized(member, "invitations.kick.kicked")
              .format(member.getName())
              .asResult();
        } else {
          return BukkitLine.localized(member, "invitations.kick.not")
              .format(member.getName())
              .formatSample()
              .formatSample()
              .asResult();
        }
      } else {
        return BukkitLine.localized(member, "invitations.invite.not-leader")
            .formatSample()
            .asResult();
      }
    } else if (optionalTeam.isPresent() && !optionalTeam.get().equals(memberTeam.orElse(null))) {
      return BukkitLine.localized(member, "invitations.kick.not-same").formatSample().asResult();
    } else {
      return BukkitLine.localized(member, "invitations.invite.no-team").formatSample().asResult();
    }
  }

  @Command(
      aliases = "leave",
      description = "Leave your team",
      permission = "invites.teams.leave",
      async = true)
  public Result leave(TeamMember member) {
    if (member.getTeam().isPresent()) {
      if (member.getRole().isPresent() && member.getRole().get() != TeamRole.LEADER) {
        if (member.leaveTeam()) {
          return BukkitLine.localized(member, "invitations.leave.left").asResult();
        } else {
          return BukkitLine.localized(member, "invitations.leave.not").formatSample().asResult();
        }
      } else {
        return BukkitLine.localized(member, "invitations.leave.leader").formatSample().asResult();
      }
    } else {
      return BukkitLine.localized(member, "invitations.invite.no-team").formatSample().asResult();
    }
  }

  @Command(
      aliases = "rename",
      description = "Rename your team",
      permission = "invites.teams.rename",
      async = true)
  public Result rename(
      TeamMember member,
      @Multiple @Required(name = "name", description = "The new name of the team") String name) {
    if (name.length() > 16)
      return BukkitLine.localized(member, "invitations.create.long-name").formatSample().asResult();
    Optional<? extends Team> team = member.getTeam();
    Optional<TeamRole> role = member.getRole();
    Optional<? extends Team> optionalTeam =
        Invites.getLoader().getSubloader(TeamsSubloader.class).getTeam(name);
    if (team.isPresent()
        && !optionalTeam.isPresent()
        && (role.isPresent()
            && (role.get() == TeamRole.LEADER || role.get() == TeamRole.SUBLEADER))) {
      if (team.get().rename(name)) {
        return BukkitLine.localized(member, "invitations.rename.renamed").format(name).asResult();
      } else {
        return BukkitLine.localized(member, "invitations.rename.not").formatSample().asResult();
      }
    } else if (team.isPresent() && optionalTeam.isPresent()) {
      return BukkitLine.localized(member, "invitations.create.already-name")
          .format(name)
          .formatSample()
          .asResult();
    } else if (team.isPresent() && (!role.isPresent() || role.get() == TeamRole.NORMAL)) {
      return BukkitLine.localized(member, "invitations.invite.not-leader")
          .formatSample()
          .asResult();
    } else {
      return BukkitLine.localized(member, "invitations.invite.no-team").formatSample().asResult();
    }
  }

  @Command(
      aliases = "disband",
      description = "Disband a team",
      permission = "invites.teams.disband",
      async = true)
  public Result disband(TeamMember member, Team team) {
    boolean isLeader = member.getRole().isPresent() && member.getRole().get() == TeamRole.LEADER;
    if (isLeader && team.disband()) {
      return BukkitLine.localized(member, "invitations.disband.done").asResult();
    } else if (!isLeader) {
      return BukkitLine.localized(member, "invitations.invite.not-leader")
          .formatSample()
          .asResult();
    } else {
      return BukkitLine.localized(member, "invitations.disband.not").formatSample().asResult();
    }
  }

  @Command(
      aliases = "view",
      description = "View a team",
      permission = "invites.teams.view",
      async = true)
  public Result view(
      Player viewer,
      @Required(name = "team", description = "The team to view") @Multiple Team team) {
    // TODO send a list to console sender
    viewer.closeInventory();
    viewer.openInventory(Factory.team(viewer, team).getInventory());
    return new Result();
  }

  @Command(
      aliases = "manage",
      description = "Open a UI to manage your team",
      permission = "invites.teams.mange")
  public void manage(TeamMember member) {
    member
        .getPlayer()
        .ifPresent(
            player -> {
              player.closeInventory();
              if (member.getRole().isPresent() && member.getRole().get() == TeamRole.LEADER) {
                player.openInventory(Factory.manageTeam().getInventory());
              } else {
                if (member.getTeam().isPresent()) {
                  member.localized("invitations.invite.not-leader");
                } else {
                  member.localized("invitations.invite.no-team");
                }
              }
            });
  }

  public static class Parent extends StarboxParentCommand {

    public Parent(@NonNull CommandManager manager) {
      super(
          "teams",
          "Opens a UI to view and manage teams",
          "<teams>",
          Arrays.asList("team", "t"),
          false,
          manager);
    }

    @Override
    public Result execute(@NonNull CommandContext context) {
      CommandSender sender = context.getSender();
      if (sender instanceof Player) {
        Player player = (Player) sender;
        player.closeInventory();
        player.openInventory(Factory.teams(player).getInventory());
        return new Result();
      }
      return Result.of(context.getMessagesProvider().playersOnly(context));
    }

    @Override
    public String getPermission() {
      return "invites.teams";
    }
  }

  public static class Factory {

    @NonNull
    public static ItemBuilder teamsItem() {
      return new ItemBuilder(Material.BOOK)
          .withMeta(
              meta -> {
                meta.setName("&9&lTeams").setLore("&7View and manage teams");
              });
    }

    @NonNull
    public static PaginatedInventory teams(@NonNull Player viewer) {
      TeamMember member = TeamMember.of(viewer);
      PaginatedInventory inventory =
          new PaginatedInventory(
                  CustomInventory.EXTRA_LARGE, BukkitUtils.format("&9&lTeams &r&9%page%&8/&7%max%"))
              .addDefaultToolbar();
      Invites.getLoader().getSubloader(MembersSubloader.class).getLeaders().stream()
          .map(
              leader -> {
                Team team = leader.getTeam().orElseThrow(NullPointerException::new);
                return Factory.teamButton(leader, team)
                    .listen(new CommandButtonListener("teams view " + team.getName()))
                    .build();
              })
          .forEach(inventory::add);
      if (member.getRole().isPresent() && member.getRole().get() == TeamRole.LEADER) {
        inventory.setToolbar(
            4,
            Factory.manageTeamButton(member)
                .listen(new CommandButtonListener("teams manage"))
                .build());
      }
      return inventory;
    }

    @NonNull
    public static ButtonBuilder teamButton(@NonNull TeamMember leader, @NonNull Team team) {
      return Button.builder(
          Factory.builder(leader.getOffline())
              .withMeta(meta -> meta.setName("&e" + team.getName()))
              .setAmount(team.getMembers().size()));
    }

    @NonNull
    private static ButtonBuilder manageTeamButton(@NonNull TeamMember member) {
      return Button.builder(
          new ItemBuilder(Material.ENDER_PORTAL_FRAME)
              .withMeta(meta -> meta.setName("&5Manage your team")));
    }

    @NonNull
    public static CustomInventory manageTeam() {
      return new CustomInventory(
              CustomInventory.EXTRA_SMALL, BukkitUtils.format("&5Manage your team"))
          .set(
              3,
              Button.builder(
                      new ItemBuilder(Materials.getWritableBook())
                          .withMeta(meta -> meta.setName("&3Rename")))
                  .build())
          .set(
              5,
              Button.builder(
                      new ItemBuilder(Material.DIAMOND_AXE)
                          .withMeta(meta -> meta.setName("&4Kick")))
                  .build());
    }

    @NonNull
    public static PaginatedInventory team(@NonNull Player viewer, @NonNull Team team) {
      PaginatedInventory inventory = new PaginatedInventory(team.getName()).addDefaultToolbar();
      team.getMembers()
          .forEach(
              member -> {
                inventory.add(
                    Button.builder(
                            Factory.builder(member.getOffline())
                                .withMeta(
                                    meta -> {
                                      meta.setName("&6" + member.getName());
                                      meta.setLore(
                                          BukkitUtils.format(
                                              "&7Role &b{0}",
                                              member
                                                  .getRole()
                                                  .orElse(TeamRole.NORMAL)
                                                  .toString()
                                                  .toLowerCase()));
                                    }))
                        .build());
              });
      return inventory;
    }

    @NonNull
    public static ItemBuilder builder(@NonNull OfflinePlayer player) {
      return new ItemBuilder(Material.SKULL_ITEM)
          .withMeta(
              meta -> {
                if (meta instanceof SkullMetaBuilder) {
                  ((SkullMetaBuilder) meta).setOwner(player);
                }
              });
    }
  }
}
