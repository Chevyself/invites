package me.googas.invites.commands;

import java.util.Collections;
import lombok.NonNull;
import me.googas.commands.annotations.Free;
import me.googas.commands.annotations.Multiple;
import me.googas.commands.annotations.Required;
import me.googas.commands.bukkit.CommandManager;
import me.googas.commands.bukkit.annotations.Command;
import me.googas.commands.bukkit.result.Result;
import me.googas.invites.Team;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.starbox.BukkitLine;
import me.googas.starbox.builders.MapBuilder;
import me.googas.starbox.commands.StarboxParentCommand;
import me.googas.starbox.modules.channels.Channel;

public class ManagerCommand {

  @Command(
      aliases = "rename",
      description = "Rename a team",
      permission = "invites.manager.rename",
      async = true)
  public Result rename(
      Channel channel,
      @Required(name = "team", description = "The team to rename") Team team,
      @Required(name = "name", description = "The new name of the team") @Multiple String name) {
    if (team.rename(name)) {
      return BukkitLine.localized(channel, "invitations.manager.rename.renamed")
          .format(MapBuilder.of("team", team.getName()).put("name", name).build())
          .asResult();
    } else {
      return BukkitLine.localized(channel, "invitations.manager.rename.not")
          .format(MapBuilder.of("team", team.getName()).put("name", name).build())
          .asResult();
    }
  }

  @Command(
      aliases = "move",
      description = "Move a player to a team",
      permission = "invites.manager.move",
      async = true)
  public Result move(
      Channel channel,
      @Required(name = "player", description = "The player to move") TeamMember player,
      @Free(name = "team", description = "The team to move the player to") Team team) {
    String name = player.getName();
    if (team == null) {
      if (player.leaveTeam()) {
        return BukkitLine.localized(channel, "invitations.manager.move.left.success")
            .format(name)
            .asResult();
      } else {
        return BukkitLine.localized(channel, "invitations.manager.move.left.not")
            .format(name)
            .asResult();
      }
    } else {
      if (player.setTeam(team, TeamRole.NORMAL)) {
        return BukkitLine.localized(channel, "invitations.manager.move.success")
            .format(MapBuilder.of("team", team.getName()).put("member", name).build())
            .asResult();
      } else {
        return BukkitLine.localized(channel, "invitations.manager.move.not")
            .format(MapBuilder.of("team", team.getName()).put("member", name).build())
            .asResult();
      }
    }
  }

  public static class Parent extends StarboxParentCommand {

    public Parent(@NonNull CommandManager manager) {
      super(
          "teamManager",
          "Allows to manage teams",
          "teamManager|tm <subcommand>",
          Collections.singletonList("tm"),
          false,
          manager);
    }

    @Override
    public String getPermission() {
      return "invites.manager";
    }
  }
}
