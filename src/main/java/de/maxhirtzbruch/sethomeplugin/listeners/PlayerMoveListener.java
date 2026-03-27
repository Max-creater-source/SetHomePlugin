package de.maxhirtzbruch.sethomeplugin.listeners;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens to player movement to cancel pending teleports if move-cancel is enabled.
 * The actual cancellation logic lives in TeleportManager (checked on tick),
 * so this listener only fires on block-to-block movement for efficiency.
 */
public class PlayerMoveListener implements Listener {

    private final SetHomePlugin plugin;

    public PlayerMoveListener(SetHomePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isCancelOnMove()) return;

        // Only care about block-level movement (reduces event spam)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // The TeleportManager's BukkitRunnable already detects movement via
        // distance checks every second – nothing extra needed here.
        // This listener is a lightweight hook in case you want to extend behaviour.
    }
}
