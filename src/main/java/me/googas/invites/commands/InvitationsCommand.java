package me.googas.invites.commands;

import java.util.Optional;
import me.googas.commands.annotations.Required;
import me.googas.commands.bukkit.annotations.Command;
import me.googas.commands.bukkit.result.Result;
import me.googas.invites.InvitationStatus;
import me.googas.invites.InvitationsSubloader;
import me.googas.invites.Invites;
import me.googas.invites.Team;
import me.googas.invites.TeamException;
import me.googas.invites.TeamInvitation;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.starbox.BukkitLine;
import me.googas.starbox.builders.MapBuilder;

public class InvitationsCommand {

  @Command(
      aliases = "accept",
      description = "Accept an invitation to join a team",
      permission = "invites.accept", async = true)
  public Result accept(
      @Required(name = "invitation", description = "The invitation to accept")
          TeamInvitation invitation) {
    if (invitation.accept()) {
      invitation
          .getLeader()
          .localized("invitations.accept.accepted", invitation.getInvited().getName());
      return BukkitLine.localized(invitation.getInvited(), "invitations.accept.done").asResult();
    } else {
      return BukkitLine.localized(invitation.getInvited(), "invitations.accept.not").asResult();
    }
  }

  @Command(
      aliases = "deny",
      description = "Deny an invitation to join a team",
      permission = "invites.deny", async = true)
  public Result deny(
      @Required(name = "invitation", description = "The invitation to denny")
          TeamInvitation invitation) {
    if (invitation.deny()) {
      return BukkitLine.localized(invitation.getInvited(), "invitations.deny.done").asResult();
    } else {
      return BukkitLine.localized(invitation.getInvited(), "invitations.deny.not").asResult();
    }
  }

  @Command(
      aliases = "invite",
      description = "Invite a new member to your team",
      permission = "invites.invite", async = true)
  public Result invite(
      TeamMember leader,
      @Required(name = "member", description = "The member to invite") TeamMember member) {
    InvitationsSubloader invitations = Invites.getLoader().getSubloader(InvitationsSubloader.class);
    Optional<? extends Team> team = leader.getTeam();
    Optional<TeamRole> role = leader.getRole();
    Optional<? extends Team> memberTeam = member.getTeam();
    Optional<? extends TeamInvitation> invitation =
        invitations.getInvitation(member, leader, InvitationStatus.WAITING);
    if (team.isPresent()
        && !invitation.isPresent()
        && (!memberTeam.isPresent() || !team.get().equals(memberTeam.get()))
        && (role.isPresent()
            && (role.get() == TeamRole.LEADER || role.get() == TeamRole.SUBLEADER))) {
      try {
        invitations.createInvitation(leader, member);
        member.localized(
            "invitations.invite.received",
            MapBuilder.of("team", team.get().getName()).put("member", leader.getName()).build());
        return BukkitLine.localized(leader, "invitations.invite.sent")
            .format(member.getName())
            .asResult();
      } catch (TeamException e) {
        Invites.handle(e);
        return BukkitLine.localized(leader, "invitations.invite.exception").asResult();
      }
    } else if (team.isPresent() && (!role.isPresent() || role.get() == TeamRole.NORMAL)) {
      return BukkitLine.localized(leader, "invitations.invite.not-leader").asResult();
    } else if (team.isPresent() && memberTeam.isPresent() && team.get().equals(memberTeam.get())) {
      return BukkitLine.localized(leader, "invitations.invite.same-team").asResult();
    } else if (invitation.isPresent()) {
      return BukkitLine.localized(leader, "invitations.invite.already")
          .format(member.getName())
          .asResult();
    } else {
      return BukkitLine.localized(leader, "invitations.invite.no-team").asResult();
    }
  }
}
