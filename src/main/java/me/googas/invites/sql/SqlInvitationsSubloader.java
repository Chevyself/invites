package me.googas.invites.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import me.googas.invites.InvitationStatus;
import me.googas.invites.InvitationsSubloader;
import me.googas.invites.Invites;
import me.googas.invites.Team;
import me.googas.invites.TeamException;
import me.googas.invites.TeamInvitation;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.lazy.sql.LazySQL;
import me.googas.lazy.sql.LazySQLSubloader;
import me.googas.lazy.sql.LazySQLSubloaderBuilder;

public class SqlInvitationsSubloader extends LazySQLSubloader implements InvitationsSubloader {

  @NonNull private final LazySchema schema;

  /**
   * Start the subloader.
   *
   * @param parent the sql parent
   * @param schema
   */
  protected SqlInvitationsSubloader(@NonNull LazySQL parent, @NonNull LazySchema schema) {
    super(parent);
    this.schema = schema;
  }

  public boolean updateStatus(
      @NonNull SqlTeamInvitation invitation, @NonNull InvitationStatus status) {
    try {
      return this.formatStatement(
                  "UPDATE `invitations` SET `status`='{0}' WHERE `id`={1};",
                  status, invitation.getId())
              .executeUpdate()
          > 0;
    } catch (SQLException e) {
      Invites.handle(e, () -> "Could not update status for invitation " + invitation);
    }
    return false;
  }

  @NonNull
  private Collection<SqlTeamInvitation> getInvitations(@NonNull ResultSet resultSet)
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
  public @NonNull LazySQLSubloader createTable() throws SQLException {
    this.statementOf(this.schema.getSql("invitations.create-table")).execute();
    return this;
  }

  @Override
  public Optional<SqlTeamInvitation> getInvitation(
      @NonNull TeamMember invited, @NonNull TeamMember leader, @NonNull InvitationStatus status) {
    return Optional.ofNullable(
        this.parent
            .getCache()
            .get(
                SqlTeamInvitation.class,
                invitation ->
                    invitation.getInvited().equals(invited)
                        && invitation.getLeader().equals(leader)
                        && invitation.getStatus().equals(status))
            .orElseGet(
                () -> {
                  try {
                    ResultSet resultSet =
                        this.formatStatement(
                                "SELECT * FROM `invitations` WHERE `invited`='{0}' AND `leader`='{1}' AND `status`='{2}';",
                                invited.getUniqueId(), leader.getUniqueId(), status)
                            .executeQuery();
                    if (resultSet.next()) {
                      SqlTeamInvitation invitation = SqlTeamInvitation.of(resultSet);
                      this.parent.getCache().add(invitation);
                      return invitation;
                    }
                  } catch (SQLException e) {
                    Invites.handle(
                        e, () -> "Could not get invitation from " + invited + " to " + leader);
                  }
                  return null;
                }));
  }

  @Override
  public @NonNull TeamInvitation createInvitation(
      @NonNull TeamMember leader, @NonNull TeamMember member) throws TeamException {
    try {
      UUID leaderUuid = leader.getUniqueId();
      UUID memberUuid = member.getUniqueId();
      SqlTeamInvitation invitation =
          new SqlTeamInvitation(0, leaderUuid, memberUuid, InvitationStatus.WAITING);
      PreparedStatement statement =
          this.statementOf(
              "INSERT INTO `invitations`(`invited`, `leader`, `status`) VALUES('{0}', '{1}', '{2}');",
              Statement.RETURN_GENERATED_KEYS, memberUuid, leaderUuid, InvitationStatus.WAITING);
      statement.executeUpdate();
      this.schema.updateId(this, statement, invitation);
      this.parent.getCache().add(invitation);
      return invitation;
    } catch (SQLException e) {
      throw new TeamException("Invitation could not be created due to a SQLException", e);
    }
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
    try {
      this.formatStatement(
              "UPDATE `invitations` SET `status`='{0}' WHERE `leader`='{1}';",
              InvitationStatus.CANCELLED, leader.getUniqueId())
          .executeUpdate();
      return true;
    } catch (SQLException e) {
      Invites.handle(e, () -> "Could not cancel invitations for " + leader);
      return false;
    }
  }

  @Override
  public @NonNull Collection<? extends TeamInvitation> getInvitations(
      @NonNull TeamMember member, @NonNull InvitationStatus status) {
    try {
      return this.getInvitations(
          this.formatStatement(
                  "SELECT * FROM `invitations` WHERE `invited`='{0}' AND `status`='{1}';",
                  member.getUniqueId(), status)
              .executeQuery());
    } catch (SQLException e) {
      Invites.handle(
          e, () -> "There's been an error while trying to get invitations for " + member);
    }
    return new ArrayList<>();
  }

  public static class Builder implements LazySQLSubloaderBuilder {

    @NonNull private final LazySchema schema;

    public Builder(@NonNull LazySchema schema) {
      this.schema = schema;
    }

    @Override
    public SqlInvitationsSubloader build(@NonNull LazySQL parent) {
      return new SqlInvitationsSubloader(parent, this.schema);
    }
  }
}
