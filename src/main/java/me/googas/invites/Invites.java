package me.googas.invites;

import lombok.NonNull;
import me.googas.lazy.Loader;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Invites {

    private static InvitesPlugin plugin;

    public static void handle(@NonNull Exception exception, @NonNull Supplier<String> supplier) {
        Invites.getLogger().log(Level.SEVERE, exception, supplier);
    }

    public static void handle(@NonNull Exception exception) {
        Invites.handle(exception, () -> "");
    }

    public static void setPlugin(InvitesPlugin plugin) {
        if (plugin != null && Invites.plugin != null) throw new IllegalStateException("Plugin has been initialized already");
        Invites.plugin = plugin;
    }

    @NonNull
    public static InvitesPlugin getPlugin() {
        return Objects.requireNonNull(Invites.plugin, "Plugin has not been initialized");
    }

    @NonNull
    public static Loader getLoader() {
        return Invites.getPlugin().getLoader().orElseThrow(() -> new NullPointerException("There's no loader"));
    }

    public static Logger getLogger() {
        return Invites.getPlugin().getLogger();
    }
}
