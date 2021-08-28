package me.googas.invites.modules.compatibilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.googas.commands.bukkit.StarboxBukkitCommand;
import me.googas.commands.bukkit.context.CommandContext;
import me.googas.commands.providers.type.StarboxContextualProvider;
import me.googas.starbox.compatibilities.Compatibility;
import me.googas.starbox.modules.Module;
import org.bukkit.plugin.Plugin;

public class PGMCompatibility implements Compatibility {

  @Getter @Setter private boolean enabled;

  @Override
  public @NonNull List<Module> getModules(@NonNull Plugin plugin) {
    return Collections.singletonList(new ObserverToolsModule());
  }

  @Override
  public Collection<StarboxContextualProvider<?, CommandContext>> getProviders() {
    return new ArrayList<>();
  }

  @Override
  public @NonNull Collection<StarboxBukkitCommand> getCommands() {
    return new ArrayList<>();
  }

  @Override
  public @NonNull String getName() {
    return "PGM";
  }
}
