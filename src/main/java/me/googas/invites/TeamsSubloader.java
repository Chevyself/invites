package me.googas.invites;

import java.util.Optional;
import lombok.NonNull;
import me.googas.lazy.Subloader;

public interface TeamsSubloader extends Subloader {

  @NonNull
  Optional<? extends Team> createTeam(@NonNull String name, @NonNull TeamMember member);

  @NonNull
  Optional<? extends Team> getTeam(int id);

  @NonNull
  Optional<? extends Team> getTeam(@NonNull String name);
}
