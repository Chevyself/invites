package me.googas.invites;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import me.googas.commands.bukkit.AnnotatedCommand;
import me.googas.commands.bukkit.CommandManager;
import me.googas.commands.bukkit.messages.BukkitMessagesProvider;
import me.googas.commands.bukkit.messages.MessagesProvider;
import me.googas.commands.bukkit.providers.registry.BukkitProvidersRegistry;
import me.googas.invites.commands.InvitationsCommand;
import me.googas.invites.commands.ManagerCommand;
import me.googas.invites.commands.TeamsCommand;
import me.googas.invites.commands.providers.TeamInvitationProvider;
import me.googas.invites.commands.providers.TeamMemberProvider;
import me.googas.invites.commands.providers.TeamProvider;
import me.googas.invites.modules.NotificationsModule;
import me.googas.invites.modules.compatibilities.PGMCompatibility;
import me.googas.invites.sql.PropertiesSchemaSupplier;
import me.googas.invites.sql.SqlInvitationsSubloader;
import me.googas.invites.sql.SqlMembersSubloader;
import me.googas.invites.sql.SqlTeamsSubloader;
import me.googas.io.StarboxFile;
import me.googas.lazy.Loader;
import me.googas.lazy.sql.LazySQL;
import me.googas.lazy.sql.LazySchema;
import me.googas.net.cache.MemoryCache;
import me.googas.starbox.BukkitYamlLanguage;
import me.googas.starbox.Starbox;
import me.googas.starbox.compatibilities.Compatibility;
import me.googas.starbox.compatibilities.CompatibilityManager;
import me.googas.starbox.modules.ModuleRegistry;
import me.googas.starbox.modules.language.LanguageModule;
import me.googas.starbox.scheduler.Scheduler;
import me.googas.starbox.time.StarboxBukkitScheduler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class InvitesPlugin extends JavaPlugin {

  @NonNull private final ModuleRegistry modules = new ModuleRegistry(this);
  @NonNull private final MessagesProvider messagesProvider = new BukkitMessagesProvider();
  @NonNull private final Scheduler scheduler = new StarboxBukkitScheduler(this);
  @NonNull @Getter private final MemoryCache cache = new MemoryCache();

  @NonNull
  private final CommandManager commandManager =
      new CommandManager(
          this, new BukkitProvidersRegistry(this.messagesProvider), this.messagesProvider);

  private Loader loader;

  @NonNull
  public Optional<Loader> getLoader() {
    return Optional.ofNullable(this.loader);
  }

  @Override
  public void onEnable() {
    Invites.setPlugin(this);
    this.cache.register(this.scheduler);
    ClassLoader loader = this.getClassLoader();
    StarboxFile pluginFolder = StarboxFile.of(this.getDataFolder());
    this.saveDefaultConfig();
    FileConfiguration config = this.getConfig();
    try {
      String url = config.getString("url", "file");
      LazySQL.LazySQLBuilder builder;
      if (url.equals("file")) {
        Class.forName("org.sqlite.JDBC");
        builder =
            LazySQL.at(
                new StarboxFile(pluginFolder, "database"),
                new LazySchema(
                    LazySchema.Type.SQLITE,
                    PropertiesSchemaSupplier.of(loader.getResource("schemas/sqlite.properties"))));
      } else {
        Class.forName("com.mysql.cj.jdbc.Driver");
        builder =
            LazySQL.at("jdbc:" + config.getString("url"))
                .setSchema(
                    new LazySchema(
                        LazySchema.Type.SQL,
                        PropertiesSchemaSupplier.of(loader.getResource("schemas/sql.properties"))));
      }
      this.loader =
          builder
              .add(
                  new SqlInvitationsSubloader.Builder(),
                  new SqlMembersSubloader.Builder(),
                  new SqlTeamsSubloader.Builder())
              .cache(this.cache)
              .build()
              .start();
    } catch (SQLException | ClassNotFoundException e) {
      Invites.handle(e, () -> "Could not connect to database");
      this.setEnabled(false);
    }

    TeamsCommand.Parent parentTeams = new TeamsCommand.Parent(this.commandManager);
    ManagerCommand.Parent parentManager = new ManagerCommand.Parent(this.commandManager);
    Collection<AnnotatedCommand> invitations =
        this.commandManager.parseCommands(new InvitationsCommand());
    Collection<AnnotatedCommand> subcommands =
        this.commandManager.parseCommands(new TeamsCommand());
    Collection<AnnotatedCommand> managerSubcommands =
        this.commandManager.parseCommands(new ManagerCommand());
    invitations.forEach(parentTeams::addChildren);
    subcommands.forEach(parentTeams::addChildren);
    managerSubcommands.forEach(parentManager::addChildren);
    this.commandManager.registerAll(invitations).registerAll(parentTeams, parentManager);
    this.commandManager
        .getProvidersRegistry()
        .addProviders(
            new TeamInvitationProvider(),
            new TeamMemberProvider(),
            new TeamMemberProvider(),
            new TeamProvider());

    Starbox.getModules()
        .require(LanguageModule.class)
        .register(this, BukkitYamlLanguage.of(this, "language"));
    this.modules.engage(new NotificationsModule());

    new CompatibilityManager()
        .add(new PGMCompatibility()).check().getCompatibilities().stream()
            .filter(Compatibility::isEnabled)
            .forEach(
                compatibility -> {
                  this.modules.engage(compatibility.getModules(this));
                });
    super.onEnable();
  }
}
