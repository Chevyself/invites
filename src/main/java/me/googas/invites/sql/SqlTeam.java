package me.googas.invites.sql;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.googas.invites.Invites;
import me.googas.invites.TeamRole;
import me.googas.invites.Team;
import me.googas.invites.TeamMember;
import me.googas.lazy.Loader;
import me.googas.lazy.sql.SQLElement;
import me.googas.starbox.time.Time;
import me.googas.starbox.time.unit.Unit;
import org.bukkit.OfflinePlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SqlTeam implements Team, SQLElement {

    @Getter @Setter
    private int id;
    @NonNull @Getter
    private final String name;

    public SqlTeam(int id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    public static SqlTeam of(@NonNull ResultSet resultSet) throws SQLException {
        return new SqlTeam(resultSet.getInt("id"), resultSet.getString("name"));
    }

    @Override
    public @NonNull Time getToRemove() {
        return Time.of(5, Unit.MINUTES);
    }

    @Override
    public boolean disband() {
        if (Invites.getLoader().getSubloader(SqlTeamsSubloader.class).disband(this)) {
            this.getMembers().forEach(member -> member.leaveTeam());
            return true;
        }
        return false;
    }

    @Override
    public @NonNull List<SqlTeamMember> getMembers() {
        return Invites.getLoader().getSubloader(SqlMembersSubloader.class).getMembers(this);
    }
}
