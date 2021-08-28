package me.googas.invites;

import java.util.Optional;
import lombok.NonNull;
import me.googas.lazy.Subloader;

public interface TeamsSubloader extends Subloader {

  @NonNull
  Team createTeam(@NonNull String name, @NonNull TeamMember member) throws TeamException;

  @NonNull
  Optional<? extends Team> getTeam(int id);

  @NonNull
  Optional<? extends Team> getTeam(@NonNull String name);
}
