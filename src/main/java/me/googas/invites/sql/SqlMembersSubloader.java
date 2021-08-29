package me.googas.invites.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import me.googas.invites.MembersSubloader;
import me.googas.invites.Team;
import me.googas.invites.TeamRole;
import me.googas.lazy.sql.LazySQL;
import me.googas.lazy.sql.LazySQLSubloader;
import me.googas.lazy.sql.LazySQLSubloaderBuilder;
import org.bukkit.OfflinePlayer;

public class SqlMembersSubloader extends LazySQLSubloader implements MembersSubloader {

  /**
   * Start the subloader.
   *
   * @param parent the sql parent
   */
  protected SqlMembersSubloader(@NonNull LazySQL parent) {
    super(parent);
  }

  public @NonNull List<SqlTeamMember> getMembers(@NonNull Team team) {
    return this.statement("SELECT * FROM `members` WHERE `team`=?;").execute(statement -> {
      statement.setInt(1, team.getId());
      return this.getMembers(statement.executeQuery());
    }).orElseGet(ArrayList::new);
  }

  @NonNull
  private List<SqlTeamMember> getMembers(@NonNull ResultSet resultSet) throws SQLException {
    List<SqlTeamMember> loaded = new ArrayList<>();
    while (resultSet.next()) {
      loaded.add(SqlTeamMember.of(resultSet));
    }
    Set<UUID> matched = new HashSet<>();
    loaded.removeIf(
        member -> {
          if (this.parent.getCache().contains(member)) {
            matched.add(member.getUniqueId());
            return true;
          }
          return false;
        });
    loaded.forEach(member -> this.parent.getCache().add(member));
    loaded.addAll(
        this.parent
            .getCache()
            .getMany(SqlTeamMember.class, member -> matched.contains(member.getUniqueId())));
    return loaded;
  }

  public boolean setTeam(@NonNull SqlTeamMember member, Team team, TeamRole role) {
    return this.statement("UPDATE `members` SET `team`=?, `role`=? WHERE `uuid`=?;").execute(statement -> {
      if (team == null) {
        statement.setNull(1, Types.INTEGER);
      } else {
        statement.setInt(1, team.getId());
      }
      if (role == null) {
        statement.setNull(2, Types.VARCHAR);
      } else {
        statement.setString(2, role.toString());
      }
      statement.setString(3, member.getUniqueId().toString());
      statement.executeUpdate();
      return true;
    }).orElse(false);
  }

  @NonNull
  public SqlTeamMember getMember(@NonNull OfflinePlayer player) {
    SqlTeamMember sqlMember =
        this.parent
            .getCache()
            .get(
                SqlTeamMember.class,
                catchable -> catchable.getUniqueId().equals(player.getUniqueId()),
                true)
            .orElseGet(
                () -> this.statement("SELECT * FROM `members` WHERE `uuid`=? LIMIT 1;").execute(statement -> {
                  statement.setString(1, player.getUniqueId().toString());
                  ResultSet query = statement.executeQuery();
                  if (query.next()) {
                      return SqlTeamMember.of(query);
                  } else {
                    return this.statement("INSERT INTO `members`(`uuid`) VALUES(?);").execute(insert -> {
                      insert.setString(1, player.getUniqueId().toString());
                      insert.execute();
                      SqlTeamMember member = new SqlTeamMember(player.getUniqueId(), -1, null);
                      this.parent.getCache().add(member);
                      return member;
                    }).orElseThrow(() -> new IllegalStateException("Could not create a new user"));
                  }
                }).orElse(null));
    return Objects.requireNonNull(
        sqlMember,
        "There seems to been an error while trying to get a member for: " + player.getUniqueId());
  }

  @Override
  public @NonNull List<SqlTeamMember> getLeaders() {
    return this.statement("SELECT * FROM `members` WHERE `role`=?;").execute(statement -> {
      statement.setString(1, TeamRole.LEADER.toString());
      return this.getMembers(statement.executeQuery());
    }).orElseGet(ArrayList::new);
  }

  @Override
  public @NonNull SqlMembersSubloader createTable() {
    this.statement("CREATE TABLE IF NOT EXISTS `members` ("
            + "`uuid` VARCHAR(36) NOT NULL,"
            + "`team` INT DEFAULT NULL,"
            + "`role` VARCHAR(50) DEFAULT 'NORMAL',"
            + "PRIMARY KEY (`uuid`));").execute(PreparedStatement::execute);
    return this;
  }

  public static class Builder implements LazySQLSubloaderBuilder {

    @Override
    @NonNull
    public SqlMembersSubloader build(@NonNull LazySQL parent) {
      return new SqlMembersSubloader(parent);
    }
  }
}
