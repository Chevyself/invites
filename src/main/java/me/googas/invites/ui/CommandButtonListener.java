package me.googas.invites.ui;

import lombok.NonNull;
import me.googas.starbox.modules.ui.ButtonListener;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;

@Deprecated
public class CommandButtonListener implements ButtonListener {

    @NonNull
    private final String command;

    public CommandButtonListener(@NonNull String command) {
        this.command = command;
    }

    @Override
    public void onClick(@NonNull InventoryClickEvent event) {
        event.setCancelled(true);
        Bukkit.dispatchCommand(event.getWhoClicked(), this.command);
    }
}
