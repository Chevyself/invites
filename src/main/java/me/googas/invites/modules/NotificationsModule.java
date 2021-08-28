package me.googas.invites.modules;

import lombok.NonNull;
import me.googas.invites.InvitationStatus;
import me.googas.invites.InvitationsSubloader;
import me.googas.invites.Invites;
import me.googas.invites.MembersSubloader;
import me.googas.invites.Team;
import me.googas.invites.TeamMember;
import me.googas.starbox.builders.MapBuilder;
import me.googas.starbox.modules.Module;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class NotificationsModule implements Module {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    TeamMember member =
        Invites.getLoader().getSubloader(MembersSubloader.class).getMember(event.getPlayer());
    Invites.getLoader()
        .getSubloader(InvitationsSubloader.class)
        .getInvitations(member, InvitationStatus.WAITING)
        .forEach(
            invitation -> {
              String name = invitation.getLeader().getTeam().map(Team::getName).orElse("deleted");
              member.localized(
                  "invitations.invite.received",
                  MapBuilder.of("team", name)
                      .put("member", invitation.getLeader().getName())
                      .build());
            });
  }

  @Override
  public @NonNull String getName() {
    return "notifications";
  }
}
