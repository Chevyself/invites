package me.googas.invites.commands;

import me.googas.commands.annotations.Required;
import me.googas.commands.bukkit.annotations.Command;
import me.googas.commands.bukkit.result.Result;
import me.googas.invites.InvitationsSubloader;
import me.googas.invites.Invites;
import me.googas.invites.Team;
import me.googas.invites.TeamException;
import me.googas.invites.TeamInvitation;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;

import java.util.Optional;

public class InvitationsCommand {

    @Command(aliases = "accept", description = "Accept an invitation to join a team", permission = "invites.accept")
    public Result accept(@Required(name = "invitation", description = "The invitation to accept") TeamInvitation invitation) {
        // TODO if
        invitation.accept();
        return new Result();
    }

    @Command(aliases = "deny", description = "Deny an invitation to join a team", permission = "invites.deny")
    public Result deny(@Required(name = "invitation", description = "The invitation to denny") TeamInvitation invitation) {
        invitation.deny();
        return new Result();
    }

    @Command(aliases = "invite", description = "Invite a new member to your team", permission = "invites.invite")
    public Result invite(TeamMember leader, @Required(name = "member", description = "The member to invite") TeamMember member) throws TeamException {
        Optional<? extends Team> team = leader.getTeam();
        Optional<TeamRole> role = leader.getRole();
        Optional<? extends Team> memberTeam = member.getTeam();
        if (team.isPresent() && (!memberTeam.isPresent() || !team.get().equals(memberTeam.get())) && (role.isPresent() && (role.get() == TeamRole.LEADER || role.get() == TeamRole.SUBLEADER))) {
            Invites.getLoader().getSubloader(InvitationsSubloader.class).createInvitation(leader, member);
            member.getPlayer().ifPresent(player -> {
                // TODO send message
            });
            return new Result("&7Invitation has been sent");
        } else if (team.isPresent() && (!role.isPresent() || role.get() == TeamRole.NORMAL)) {
            return new Result("&cYou are not the leader of your team");
        } else if (team.isPresent() && memberTeam.isPresent() && team.get().equals(memberTeam.get())) {
            return new Result("&cYou are in the same team");
        }  else {
            return new Result("&cYou don't have a team");
        }
    }

}
