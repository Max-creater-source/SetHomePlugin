package de.maxhirtzbruch.sethomeplugin.gui;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import de.maxhirtzbruch.sethomeplugin.managers.ConfigManager;
import de.maxhirtzbruch.sethomeplugin.models.Home;
import de.maxhirtzbruch.sethomeplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A 3-row confirmation GUI that pops up before deleting a home.
 *
 * Layout (27 slots):
 *   11 → Confirm (green)
 *   13 → Home preview item
 *   15 → Cancel  (red)
 */
public class ConfirmGui implements InventoryHolder {

    public static final int SLOT_CONFIRM = 11;
    public static final int SLOT_HOME    = 13;
    public static final int SLOT_CANCEL  = 15;

    private final SetHomePlugin plugin;
    private final Inventory inventory;
    private final Home home;
    private final int previousPage;

    public ConfirmGui(SetHomePlugin plugin, Home home, int previousPage) {
        this.plugin       = plugin;
        this.home         = home;
        this.previousPage = previousPage;

        String title = plugin.getConfigManager().getMessageRaw(
                "gui-confirm-title", "name", home.getName());

        this.inventory = Bukkit.createInventory(this, 27, title);
        populate();
    }

    private void populate() {
        // Filler
        var filler = new ItemBuilder(plugin.getConfigManager().getFillerItem())
                .name(ConfigManager.colorize("&7"))
                .hideFlags()
                .build();
        for (int i = 0; i < 27; i++) inventory.setItem(i, filler);

        // Confirm button
        inventory.setItem(SLOT_CONFIRM,
                new ItemBuilder(plugin.getConfigManager().getConfirmItem())
                        .name(ConfigManager.colorize(
                                plugin.getConfigManager().getMessageRaw("gui-confirm-yes")))
                        .hideFlags()
                        .build());

        // Home preview
        inventory.setItem(SLOT_HOME,
                new ItemBuilder(plugin.getConfigManager().getHomeItem())
                        .name(ConfigManager.colorize("&e⌂ &f" + home.getName()))
                        .lore(
                                ConfigManager.colorize("&7World: &f" + home.getWorld()),
                                ConfigManager.colorize("&7X: &f" + home.getFormattedX()
                                        + " &7Y: &f" + home.getFormattedY()
                                        + " &7Z: &f" + home.getFormattedZ())
                        )
                        .hideFlags()
                        .build());

        // Cancel button
        inventory.setItem(SLOT_CANCEL,
                new ItemBuilder(plugin.getConfigManager().getCancelItem())
                        .name(ConfigManager.colorize(
                                plugin.getConfigManager().getMessageRaw("gui-confirm-no")))
                        .hideFlags()
                        .build());
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public Home getHome()        { return home; }
    public int getPreviousPage() { return previousPage; }
}
