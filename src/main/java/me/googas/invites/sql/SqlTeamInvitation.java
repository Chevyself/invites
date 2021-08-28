package me.googas.invites.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.googas.invites.InvitationStatus;
import me.googas.invites.Invites;
import me.googas.invites.TeamInvitation;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.lazy.sql.SQLElement;
import me.googas.net.cache.Catchable;
import me.googas.starbox.time.Time;
import me.googas.starbox.time.unit.Unit;
import org.bukkit.Bukkit;

public class SqlTeamInvitation implements TeamInvitation, SQLElement, Catchable {

  @NonNull private final UUID invited;
  @NonNull private final UUID leader;
  @Getter @Setter private int id;
  @NonNull @Getter private InvitationStatus status;

  public SqlTeamInvitation(
      int id, @NonNull UUID invited, @NonNull UUID leader, @NonNull InvitationStatus status) {
    this.id = id;
    this.invited = invited;
    this.leader = leader;
    this.status = status;
  }

  @NonNull
  public static SqlTeamInvitation of(@NonNull ResultSet resultSet) throws SQLException {
    return new SqlTeamInvitation(
        resultSet.getInt("id"),
        UUID.fromString(resultSet.getString("invited")),
        UUID.fromString(resultSet.getString("leader")),
        InvitationStatus.valueOf(resultSet.getString("status")));
  }

  @Override
  public @NonNull Time getToRemove() {
    return Time.of(5, Unit.MINUTES);
  }

  @Override
  public boolean accept() {
    InvitationStatus status = InvitationStatus.ACCEPTED;
    if (this.getInvited()
            .setTeam(
                this.getLeader().getTeam().orElseThrow(NullPointerException::new), TeamRole.NORMAL)
        && Invites.getLoader()
            .getSubloader(SqlInvitationsSubloader.class)
            .updateStatus(this, status)) {
      this.status = status;
      return true;
    }
    return false;
  }

  @Override
  public boolean deny() {
    InvitationStatus status = InvitationStatus.DENIED;
    if (Invites.getLoader()
        .getSubloader(SqlInvitationsSubloader.class)
        .updateStatus(this, status)) {
      this.status = status;
      return true;
    }
    return false;
  }

  @Override
  public @NonNull TeamMember getLeader() {
    return Invites.getLoader()
        .getSubloader(SqlMembersSubloader.class)
        .getMember(Bukkit.getOfflinePlayer(this.leader));
  }

  @Override
  public @NonNull TeamMember getInvited() {
    return Invites.getLoader()
        .getSubloader(SqlMembersSubloader.class)
        .getMember(Bukkit.getOfflinePlayer(this.invited));
  }
}
