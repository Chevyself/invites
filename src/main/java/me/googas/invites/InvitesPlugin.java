package me.googas.invites;

import lombok.NonNull;
import me.googas.commands.bukkit.AnnotatedCommand;
import me.googas.commands.bukkit.CommandManager;
import me.googas.commands.bukkit.messages.BukkitMessagesProvider;
import me.googas.commands.bukkit.messages.MessagesProvider;
import me.googas.commands.bukkit.providers.registry.BukkitProvidersRegistry;
import me.googas.invites.commands.InvitationsCommand;
import me.googas.invites.commands.TeamCommand;
import me.googas.invites.commands.providers.TeamMemberProvider;
import me.googas.invites.commands.providers.TeamProvider;
import me.googas.invites.sql.LazySchema;
import me.googas.invites.sql.SqlMembersSubloader;
import me.googas.invites.sql.SqlTeamsSubloader;
import me.googas.io.StarboxFile;
import me.googas.lazy.Loader;
import me.googas.lazy.sql.LazySQL;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public class InvitesPlugin extends JavaPlugin {

    @NonNull
    private final MessagesProvider messagesProvider = new BukkitMessagesProvider();
    @NonNull
    private final CommandManager commandManager = new CommandManager(this, new BukkitProvidersRegistry(this.messagesProvider), this.messagesProvider);
    private Loader loader;

    @NonNull
    public Optional<Loader> getLoader() {
        return Optional.ofNullable(this.loader);
    }

    @Override
    public void onEnable() {
        Invites.setPlugin(this);
        StarboxFile pluginFolder = StarboxFile.of(this.getDataFolder());
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        try {
            String url = config.getString("url", "file");
            LazySQL.LazySQLBuilder builder;
            if (url.equals("file")) {
                Class.forName("org.h2.Driver");
                builder = LazySQL.at(new StarboxFile(pluginFolder, "database"), "h2");
            } else {
                builder = LazySQL.at(config.getString("url"));
            }
            this.loader = builder.add(new SqlMembersSubloader.Builder(), new SqlTeamsSubloader.Builder()).build().start();
        } catch (SQLException | ClassNotFoundException e) {
            Invites.handle(e, () -> "Could not connect to database");
            this.setEnabled(false);
        }
        TeamCommand teamCommand = new TeamCommand(this.commandManager);
        Collection<AnnotatedCommand> invitations = this.commandManager.parseCommands(new InvitationsCommand());
        Collection<AnnotatedCommand> subcommands = this.commandManager.parseCommands(new TeamCommand.SubCommands());
        subcommands.forEach(teamCommand::addChildren);
        invitations.forEach(teamCommand::addChildren);
        this.commandManager.registerAll(invitations);
        this.commandManager.registerAll(subcommands);
        this.commandManager.registerAll(teamCommand);
        this.commandManager.getProvidersRegistry().addProviders(new TeamMemberProvider(), new TeamProvider());
        super.onEnable();
    }
}
