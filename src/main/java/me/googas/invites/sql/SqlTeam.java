package me.googas.invites.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.googas.invites.InvitationsSubloader;
import me.googas.invites.Invites;
import me.googas.invites.Team;
import me.googas.invites.TeamRole;
import me.googas.lazy.sql.SQLElement;
import me.googas.starbox.time.Time;
import me.googas.starbox.time.unit.Unit;

public class SqlTeam implements Team, SQLElement {

  @Getter @Setter private int id;
  @NonNull @Getter private String name;

  public SqlTeam(int id, @NonNull String name) {
    this.id = id;
    this.name = name;
  }

  public static SqlTeam of(@NonNull ResultSet resultSet) throws SQLException {
    return new SqlTeam(resultSet.getInt("id"), resultSet.getString("name"));
  }

  private boolean kickAll() {
    AtomicBoolean success = new AtomicBoolean();
    this.getMembers()
        .forEach(
            member -> {
              if (!member.leaveTeam()) success.set(false);
            });
    return true;
  }

  @Override
  public @NonNull Time getToRemove() {
    return Time.of(5, Unit.MINUTES);
  }

  @Override
  public boolean disband() {
    if (Invites.getLoader().getSubloader(InvitationsSubloader.class).cancelAll(this)
        && this.kickAll()
        && Invites.getLoader().getSubloader(SqlTeamsSubloader.class).disband(this)) {
      this.getMembers().forEach(SqlTeamMember::leaveTeam);
      return true;
    }
    return false;
  }

  @Override
  public boolean rename(@NonNull String name) {
    if (Invites.getLoader().getSubloader(SqlTeamsSubloader.class).rename(this, name)) {
      this.name = name;
      return true;
    }
    return false;
  }

  @Override
  public @NonNull Collection<SqlTeamMember> getMembers(@NonNull TeamRole... roles) {
    return this.getMembers().stream()
        .filter(
            member -> {
              if (member.getRole().isPresent()) {
                TeamRole role = member.getRole().get();
                for (TeamRole teamRole : roles) {
                  if (role == teamRole) return true;
                }
              }
              return false;
            })
        .collect(Collectors.toList());
  }

  @Override
  public @NonNull List<SqlTeamMember> getMembers() {
    return Invites.getLoader().getSubloader(SqlMembersSubloader.class).getMembers(this);
  }
}
