package me.googas.invites.modules.compatibilities;

import lombok.NonNull;
import me.googas.invites.commands.TeamsCommand;
import me.googas.starbox.Starbox;
import me.googas.starbox.modules.Module;
import me.googas.starbox.modules.ui.UIModule;
import me.googas.starbox.modules.ui.item.CommandItemButtonListener;
import me.googas.starbox.modules.ui.item.ItemButton;
import me.googas.starbox.modules.ui.item.StarboxItemButton;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.spawns.events.ObserverKitApplyEvent;

public class ObserverToolsModule implements Module {

    @NonNull
    private final ItemButton teamsButton = new StarboxItemButton(new CommandItemButtonListener("teams"), TeamsCommand.Factory.teamsItem().build());

    @EventHandler
    public void onObserverKitApply(ObserverKitApplyEvent event) {
        event.getPlayer().getInventory().setItem(3, this.teamsButton.getItem());
    }

    @Override
    public void onEnable() {
        Starbox.getModules().get(UIModule.class).ifPresent(module -> module.add(this.teamsButton));
        Module.super.onEnable();
    }

    @Override
    public @NonNull String getName() {
        return "spectator-tool";
    }
}
