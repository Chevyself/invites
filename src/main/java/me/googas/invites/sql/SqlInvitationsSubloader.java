package me.googas.invites.sql;

import lombok.NonNull;
import me.googas.invites.InvitationStatus;
import me.googas.invites.InvitationsSubloader;
import me.googas.invites.Team;
import me.googas.invites.TeamException;
import me.googas.invites.TeamInvitation;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.lazy.sql.LazySQL;
import me.googas.lazy.sql.LazySQLSubloader;
import me.googas.lazy.sql.LazySQLSubloaderBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SqlInvitationsSubloader extends LazySQLSubloader implements InvitationsSubloader {

  /**
   * Start the subloader.
   *
   * @param parent the sql parent
   */
  protected SqlInvitationsSubloader(@NonNull LazySQL parent) {
    super(parent);
  }

  public boolean updateStatus(
      @NonNull SqlTeamInvitation invitation, @NonNull InvitationStatus status) {
    return this.statement("UPDATE `invitations` SET `status`=? WHERE `id`=?;").execute(statement -> {
      statement.setString(1, status.toString());
      statement.setInt(2, invitation.getId());
      return statement.executeUpdate() > 0;
    }).orElse(false);
  }

  @NonNull
  private List<SqlTeamInvitation> getInvitations(@NonNull ResultSet resultSet)
      throws SQLException {
    List<SqlTeamInvitation> loaded = new ArrayList<>();
    while (resultSet.next()) {
      loaded.add(SqlTeamInvitation.of(resultSet));
    }
    Set<Integer> matched = new HashSet<>();
    loaded.removeIf(
        invitation -> {
          if (this.parent.getCache().contains(invitation)) {
            matched.add(invitation.getId());
            return true;
          }
          return false;
        });
    loaded.forEach(invitation -> this.parent.getCache().add(invitation));
    loaded.addAll(
        this.parent
            .getCache()
            .getMany(SqlTeamInvitation.class, invitation -> matched.contains(invitation.getId())));
    return loaded;
  }

  @Override
  public @NonNull LazySQLSubloader createTable() {
    this.statementWithKey("invitations.create-table").execute(PreparedStatement::execute);
    return this;
  }

  @Override
  public Optional<SqlTeamInvitation> getInvitation(
      @NonNull TeamMember invited, @NonNull TeamMember leader, @NonNull InvitationStatus status) {
    SqlTeamInvitation sqlTeamInvitation = this.parent
            .getCache()
            .get(
                    SqlTeamInvitation.class,
                    invitation ->
                            invitation.getInvited().equals(invited)
                                    && invitation.getLeader().equals(leader)
                                    && invitation.getStatus().equals(status))
            .orElseGet(() -> this.statement("SELECT * FROM `invitations` WHERE `invited`=? AND `leader`=? AND `status`=?;").execute(statement -> {
              statement.setString(1, invited.getUniqueId().toString());
              statement.setString(2, leader.getUniqueId().toString());
              statement.setString(3, status.toString());
              ResultSet resultSet = statement.executeQuery();
              if (resultSet.next()) {
                SqlTeamInvitation invitation = SqlTeamInvitation.of(resultSet);
                this.parent.getCache().add(invitation);
                return invitation;
              }
              return null;
            }).orElse(null));
    return Optional.ofNullable(sqlTeamInvitation);
  }

  @Override
  public @NonNull TeamInvitation createInvitation(
      @NonNull TeamMember leader, @NonNull TeamMember invited) throws TeamException {
    Optional<TeamInvitation> optional = this.statement("INSERT INTO `invitations`(`invited`, `leader`, `status`) VALUES(?, ?, ?);", Statement.RETURN_GENERATED_KEYS).execute(statement -> {
      UUID invitedUniqueId = invited.getUniqueId();
      UUID leaderUniqueId = leader.getUniqueId();
      SqlTeamInvitation invitation = new SqlTeamInvitation(-1, invitedUniqueId, leaderUniqueId, InvitationStatus.WAITING);
      statement.setString(1, invitedUniqueId.toString());
      statement.setString(2, leaderUniqueId.toString());
      statement.setString(3, InvitationStatus.WAITING.toString());
      statement.executeUpdate();
      this.parent.getSchema().updateId(statement, invitation);
      return invitation;
    });
    if (optional.isPresent()) {
      return optional.get();
    }
    throw new TeamException("Invitation could not be created");
  }

  @Override
  public boolean cancelAll(@NonNull Team team) {
    AtomicBoolean cancelled = new AtomicBoolean(true);
    team.getMembers(TeamRole.LEADER, TeamRole.SUBLEADER)
        .forEach(
            leader -> {
              if (!this.cancelAll(leader)) cancelled.set(false);
            });
    return cancelled.get();
  }

  @Override
  public boolean cancelAll(@NonNull TeamMember leader) {
    return this.statement("UPDATE `invitations` SET `status`=? WHERE `leader`=?;").execute(statement -> {
      statement.setString(1, InvitationStatus.CANCELLED.toString());
      statement.setString(2, leader.getUniqueId().toString());
      statement.executeUpdate();
      return true;
    }).orElse(false);
  }

  @Override
  public @NonNull List<SqlTeamInvitation> getInvitations(
      @NonNull TeamMember member, @NonNull InvitationStatus status) {
    return this.statement("SELECT * FROM `invitations` WHERE `invited`=? AND `status`=?;").execute(statement -> {
      statement.setString(1, member.getUniqueId().toString());
      statement.setString(2, status.toString());
      return this.getInvitations(statement.executeQuery());
    }).orElseGet(ArrayList::new);
  }

  public static class Builder implements LazySQLSubloaderBuilder {

    @Override
    public SqlInvitationsSubloader build(@NonNull LazySQL parent) {
      return new SqlInvitationsSubloader(parent);
    }
  }
}
