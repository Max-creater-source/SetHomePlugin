package de.maxhirtzbruch.sethomeplugin.listeners;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import de.maxhirtzbruch.sethomeplugin.gui.ConfirmGui;
import de.maxhirtzbruch.sethomeplugin.gui.HomesGui;
import de.maxhirtzbruch.sethomeplugin.models.Home;
import de.maxhirtzbruch.sethomeplugin.utils.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

/**
 * Handles all inventory-click interactions for HomesGui and ConfirmGui.
 */
public class GuiListener implements Listener {

    private final SetHomePlugin plugin;

    public GuiListener(SetHomePlugin plugin) {
        this.plugin = plugin;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Homes GUI
    // ──────────────────────────────────────────────────────────────────────────

    @EventHandler
    public void onHomesGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof HomesGui gui)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        // ── Navigation buttons ───────────────────────────────────
        if (slot == HomesGui.SLOT_CLOSE) {
            player.closeInventory();
            return;
        }

        if (slot == HomesGui.SLOT_PREV && gui.getPage() > 0) {
            openHomesGui(player, gui.getPage() - 1);
            return;
        }

        if (slot == HomesGui.SLOT_NEXT && gui.getPage() < gui.getTotalPages() - 1) {
            openHomesGui(player, gui.getPage() + 1);
            return;
        }

        // ── Home item clicked ────────────────────────────────────
        Home home = gui.getHomeAt(slot);
        if (home == null) return;

        switch (event.getClick()) {
            case LEFT, SHIFT_LEFT -> {
                // Teleport
                player.closeInventory();
                plugin.getTeleportManager().startTeleport(player, home);
            }
            case RIGHT, SHIFT_RIGHT -> {
                // Delete – open confirm GUI or delete directly
                if (plugin.getConfigManager().isConfirmEnabled()) {
                    player.openInventory(
                            new ConfirmGui(plugin, home, gui.getPage()).getInventory());
                } else {
                    plugin.getHomeManager().deleteHome(player.getUniqueId(), home.getName());
                    player.sendMessage(plugin.getConfigManager().getMessage(
                            "home-deleted", "name", home.getName()));
                    SoundUtil.playDeleteHome(plugin, player);
                    // Refresh GUI
                    openHomesGui(player, gui.getPage());
                }
            }
            default -> { /* ignore middle-click etc. */ }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Confirm GUI
    // ──────────────────────────────────────────────────────────────────────────

    @EventHandler
    public void onConfirmGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof ConfirmGui gui)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot == ConfirmGui.SLOT_CONFIRM) {
            Home home = gui.getHome();
            plugin.getHomeManager().deleteHome(player.getUniqueId(), home.getName());
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "home-deleted", "name", home.getName()));
            SoundUtil.playDeleteHome(plugin, player);
            // Return to homes list
            openHomesGui(player, gui.getPreviousPage());

        } else if (slot == ConfirmGui.SLOT_CANCEL) {
            // Return without deleting
            openHomesGui(player, gui.getPreviousPage());
        }
        // Clicks on other slots are simply cancelled
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Player quit – unload cache
    // ──────────────────────────────────────────────────────────────────────────

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getHomeManager().unloadPlayer(event.getPlayer().getUniqueId());
        plugin.getTeleportManager().cancelTeleport(event.getPlayer().getUniqueId());
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Helper
    // ──────────────────────────────────────────────────────────────────────────

    private void openHomesGui(Player player, int page) {
        List<Home> homes   = plugin.getHomeManager().getHomeList(player.getUniqueId());
        int        maxHomes = plugin.getLuckPermsManager().getMaxHomes(player);
        player.openInventory(
                new HomesGui(plugin, homes, page, maxHomes).getInventory());
        SoundUtil.playOpenGui(plugin, player);
    }
}
