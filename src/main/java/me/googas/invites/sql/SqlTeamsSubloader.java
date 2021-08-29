package me.googas.invites.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import lombok.NonNull;
import me.googas.invites.Invites;
import me.googas.invites.Team;
import me.googas.invites.TeamException;
import me.googas.invites.TeamMember;
import me.googas.invites.TeamRole;
import me.googas.invites.TeamsSubloader;
import me.googas.lazy.sql.LazySQL;
import me.googas.lazy.sql.LazySQLSubloader;
import me.googas.lazy.sql.LazySQLSubloaderBuilder;

public class SqlTeamsSubloader extends LazySQLSubloader implements TeamsSubloader {

  /**
   * Start the subloader.
   *
   * @param parent the sql parent
   */
  protected SqlTeamsSubloader(@NonNull LazySQL parent) {
    super(parent);
  }

  public boolean disband(@NonNull SqlTeam team) {
    return this.statement("DELETE FROM `teams` WHERE `id`=?;").execute(statement -> {
      statement.setInt(1, team.getId());
      statement.executeUpdate();
      this.parent.getCache().remove(team);
      return true;
    }).orElse(false);
  }

  public boolean rename(@NonNull SqlTeam team, @NonNull String name) {
    return this.statement("UPDATE `teams` SET `name`=? WHERE `id`=?;").execute(statement -> {
      statement.setString(1, name);
      statement.setInt(2, team.getId());
      return statement.executeUpdate() > 0;
    }).orElse(false);
  }

  @Override
  public @NonNull SqlTeamsSubloader createTable() throws SQLException {
    this.statementWithKey("teams.create-table").execute(PreparedStatement::execute);
    return this;
  }

  @Override
  public @NonNull Team createTeam(@NonNull String name, @NonNull TeamMember leader)
      throws TeamException {
    Optional<SqlTeam> optional = this.statement("INSERT INTO `teams` (`name`) VALUES(?);", Statement.RETURN_GENERATED_KEYS).execute(statement -> {
      SqlTeam team = new SqlTeam(-1, name);
      statement.setString(1, name);
      statement.executeUpdate();
      this.parent.getSchema().updateId(statement, team);
      this.parent.getCache().add(team);
      leader.setTeam(team, TeamRole.LEADER);
      return team;
    });
    if (optional.isPresent()) {
      return optional.get();
    } else {
      throw new TeamException("Team could not be created");
    }
  }

  @Override
  public @NonNull Optional<SqlTeam> getTeam(int id) {
    return Optional.ofNullable(
        this.parent
            .getCache()
            .get(SqlTeam.class, team -> team.getId() == id)
            .orElseGet(
                () ->
                  this.statement("SELECT * FROM `teams` WHERE `id`=?;").execute(statement -> {
                    statement.setInt(1, id);
                    ResultSet query = statement.executeQuery();
                    if (query.next()) {
                      SqlTeam team = SqlTeam.of(query);
                      this.parent.getCache().add(team);
                      return team;
                    }
                    return null;
                  }).orElse(null)
                ));
  }

  @Override
  public @NonNull Optional<SqlTeam> getTeam(@NonNull String name) {
    return Optional.ofNullable(
        this.parent
            .getCache()
            .get(SqlTeam.class, team -> team.getName().equalsIgnoreCase(name))
            .orElseGet(
                () ->
                  this.statement("SELECT DISTINCT * FROM `teams` WHERE LOWER(`name`) LIKE LOWER(?);").execute(statement -> {
                    statement.setString(1, name);
                    ResultSet query = statement.executeQuery();
                    if (query.next()) {
                      SqlTeam team = SqlTeam.of(query);
                      this.parent.getCache().add(team);
                      return team;
                    }
                    return null;
                  }).orElse(null)
                  ));
  }

  public static class Builder implements LazySQLSubloaderBuilder {

    @Override
    @NonNull
    public SqlTeamsSubloader build(@NonNull LazySQL parent) {
      return new SqlTeamsSubloader(parent);
    }
  }
}
