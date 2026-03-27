package de.maxhirtzbruch.sethomeplugin.managers;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import de.maxhirtzbruch.sethomeplugin.models.Home;
import de.maxhirtzbruch.sethomeplugin.utils.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles delayed teleportation with move-cancel support.
 */
public class TeleportManager {

    private final SetHomePlugin plugin;

    /** Pending teleports: playerUUID → task */
    private final Map<UUID, BukkitTask> pendingTasks     = new ConcurrentHashMap<>();
    /** Starting location to detect movement */
    private final Map<UUID, Location>   startLocations   = new ConcurrentHashMap<>();

    public TeleportManager(SetHomePlugin plugin) {
        this.plugin = plugin;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Starts a teleport to the given home.
     * If delay == 0, teleports immediately.
     */
    public void startTeleport(Player player, Home home) {
        int delay = plugin.getConfigManager().getTeleportDelay();

        // Cancel any pending teleport first
        cancelTeleport(player.getUniqueId());

        if (delay <= 0) {
            performTeleport(player, home);
            return;
        }

        // Send countdown message
        player.sendMessage(plugin.getConfigManager().getMessage(
                "teleporting",
                "name",  home.getName(),
                "delay", String.valueOf(delay)
        ));

        startLocations.put(player.getUniqueId(), player.getLocation().clone());

        BukkitTask task = new BukkitRunnable() {
            private int remaining = delay;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    cleanup(player.getUniqueId());
                    return;
                }

                // Check if player moved
                if (plugin.getConfigManager().isCancelOnMove()
                        && hasMoved(player)) {
                    player.sendMessage(plugin.getConfigManager().getMessage("teleport-cancelled"));
                    SoundUtil.playError(plugin, player);
                    cancel();
                    cleanup(player.getUniqueId());
                    return;
                }

                remaining--;

                if (remaining <= 0) {
                    performTeleport(player, home);
                    cancel();
                    cleanup(player.getUniqueId());
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);  // check every second

        pendingTasks.put(player.getUniqueId(), task);
    }

    /**
     * Called from the move listener to cancel an active teleport.
     */
    public boolean hasPendingTeleport(UUID uuid) {
        return pendingTasks.containsKey(uuid);
    }

    public void cancelTeleport(UUID uuid) {
        BukkitTask task = pendingTasks.remove(uuid);
        if (task != null) task.cancel();
        startLocations.remove(uuid);
    }

    /** Cancels ALL pending teleports – called on plugin disable. */
    public void cancelAll() {
        pendingTasks.values().forEach(BukkitTask::cancel);
        pendingTasks.clear();
        startLocations.clear();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Internals
    // ──────────────────────────────────────────────────────────────────────────

    private void performTeleport(Player player, Home home) {
        Location dest = home.toLocation();
        if (dest == null) {
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "home-not-found", "name", home.getName()));
            return;
        }

        // Particles at origin
        if (plugin.getConfigManager().isParticlesEnabled()) {
            spawnParticles(player.getLocation());
        }

        player.teleport(dest);

        // Particles at destination
        if (plugin.getConfigManager().isParticlesEnabled()) {
            spawnParticles(dest);
        }

        // Sound
        SoundUtil.playSound(plugin, player,
                plugin.getConfigManager().getTeleportSound());

        player.sendMessage(plugin.getConfigManager().getMessage(
                "teleported", "name", home.getName()));
    }

    private boolean hasMoved(Player player) {
        Location start = startLocations.get(player.getUniqueId());
        if (start == null) return false;
        Location current = player.getLocation();
        // Count as moved if more than 0.15 blocks away
        return start.distanceSquared(current) > 0.0225;
    }

    private void cleanup(UUID uuid) {
        pendingTasks.remove(uuid);
        startLocations.remove(uuid);
    }

    @SuppressWarnings("deprecation")
    private void spawnParticles(Location loc) {
        try {
            Particle particle = Particle.valueOf(
                    plugin.getConfigManager().getParticleType().toUpperCase());
            if (loc.getWorld() != null) {
                loc.getWorld().spawnParticle(particle, loc,
                        plugin.getConfigManager().getParticleCount(),
                        0.3, 0.5, 0.3, 0.05);
            }
        } catch (IllegalArgumentException ignored) {
            // Invalid particle type in config – silently skip
        }
    }
}
