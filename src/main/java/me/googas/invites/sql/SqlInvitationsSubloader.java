package me.googas.invites.sql;

import lombok.NonNull;
import me.googas.invites.InvitationStatus;
import me.googas.invites.InvitationsSubloader;
import me.googas.invites.Invites;
import me.googas.invites.TeamException;
import me.googas.invites.TeamInvitation;
import me.googas.invites.TeamMember;
import me.googas.lazy.sql.LazySQL;
import me.googas.lazy.sql.LazySQLSubloader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

public class SqlInvitationsSubloader extends LazySQLSubloader implements InvitationsSubloader {

    @NonNull
    private final LazySchema schema;

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

    public boolean updateStatus(@NonNull SqlTeamInvitation invitation, @NonNull InvitationStatus status) {
        try {
            return this.formatStatement("UPDATE `invitations` SET `status`='{0}' WHERE `id`={1};", status, invitation.getId()).executeUpdate() > 0;
        } catch (SQLException e) {
            Invites.handle(e, () -> "Could not update status for invitation " + invitation);
        }
        return false;
    }

    @Override
    public @NonNull LazySQLSubloader createTable() throws SQLException {
        this.statementOf(this.schema.getSql("invitations.create-table")).execute();
        return this;
    }

    @Override
    public Optional<SqlTeamInvitation> getInvitation(@NonNull TeamMember invited, @NonNull TeamMember leader, @NonNull InvitationStatus status) {
        return Optional.ofNullable(this.parent.getCache().get(SqlTeamInvitation.class, invitation -> invitation.getInvited().equals(invited) && invitation.getLeader().equals(leader) && invitation.getStatus().equals(status)).orElseGet(() -> {
            try {
                ResultSet resultSet = this.formatStatement("SELECT * FROM `invitations` WHERE `invited`='{0}', `leader`='{1}', status='{2}';", invited.getUniqueId(), leader.getUniqueId(), status).executeQuery();
                if (resultSet.next()) {
                    SqlTeamInvitation invitation = SqlTeamInvitation.of(resultSet);
                    this.parent.getCache().add(invitation);
                    return invitation;
                }
            } catch (SQLException e) {
                Invites.handle(e, () -> "Could not get invitation from " + invited + " to " + leader);
            }
            return null;
        }));
    }

    @Override
    public @NonNull TeamInvitation createInvitation(@NonNull TeamMember leader, @NonNull TeamMember member) throws TeamException {
        try {
            UUID leaderUuid = leader.getUniqueId();
            UUID memberUuid = member.getUniqueId();
            SqlTeamInvitation invitation = new SqlTeamInvitation(0, leaderUuid, memberUuid, InvitationStatus.WAITING);
            PreparedStatement statement = this.statementOf("INSERT INTO `invitations`(`invited`, `leader`, `status`) VALUES('{0}', '{1}', '{2}');", Statement.RETURN_GENERATED_KEYS, leaderUuid, memberUuid, InvitationStatus.WAITING);
            statement.executeUpdate();
            this.schema.updateId(this, statement, invitation);
            this.parent.getCache().add(invitation);
            return invitation;
        } catch (SQLException e) {
            throw new TeamException("Invitation could not be created due to a SQLException", e);
        }
    }
}
