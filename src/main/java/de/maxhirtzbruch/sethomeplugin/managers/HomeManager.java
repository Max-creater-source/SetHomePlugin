package de.maxhirtzbruch.sethomeplugin.managers;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import de.maxhirtzbruch.sethomeplugin.models.Home;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all home data.  Uses per-player YAML files inside plugins/SetHomePlugin/homes/.
 */
public class HomeManager {

    private final SetHomePlugin plugin;

    /** In-memory cache:  playerUUID → (homeName → Home) */
    private final Map<UUID, Map<String, Home>> homeCache = new ConcurrentHashMap<>();

    private final File homesDir;

    public HomeManager(SetHomePlugin plugin) {
        this.plugin   = plugin;
        this.homesDir = new File(plugin.getDataFolder(), "homes");
        if (!homesDir.exists()) homesDir.mkdirs();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Creates / overwrites a home.
     * Returns false if the player is already at their limit.
     */
    public boolean setHome(Player player, String name) {
        Map<String, Home> homes = getHomes(player.getUniqueId());
        int maxHomes = plugin.getLuckPermsManager().getMaxHomes(player);

        if (!homes.containsKey(name.toLowerCase()) && homes.size() >= maxHomes) {
            return false;  // limit reached
        }

        homes.put(name.toLowerCase(), new Home(player.getUniqueId(), name, player.getLocation()));
        savePlayer(player.getUniqueId());
        return true;
    }

    /** Returns null when the home doesn't exist. */
    public Home getHome(UUID uuid, String name) {
        return getHomes(uuid).get(name.toLowerCase());
    }

    /**
     * Deletes a home.
     * Returns false if the home didn't exist.
     */
    public boolean deleteHome(UUID uuid, String name) {
        Map<String, Home> homes = getHomes(uuid);
        if (homes.remove(name.toLowerCase()) == null) return false;
        savePlayer(uuid);
        return true;
    }

    /** Returns an unmodifiable view of all homes for the given player. */
    public Map<String, Home> getHomesMap(UUID uuid) {
        return Collections.unmodifiableMap(getHomes(uuid));
    }

    /** Returns a sorted list of home names for the given player. */
    public List<Home> getHomeList(UUID uuid) {
        List<Home> list = new ArrayList<>(getHomes(uuid).values());
        list.sort(Comparator.comparing(Home::getName, String.CASE_INSENSITIVE_ORDER));
        return Collections.unmodifiableList(list);
    }

    /** Returns the number of homes the player currently has. */
    public int getHomeCount(UUID uuid) {
        return getHomes(uuid).size();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Cache / IO
    // ──────────────────────────────────────────────────────────────────────────

    private Map<String, Home> getHomes(UUID uuid) {
        return homeCache.computeIfAbsent(uuid, id -> loadPlayer(id));
    }

    private Map<String, Home> loadPlayer(UUID uuid) {
        Map<String, Home> homes = new LinkedHashMap<>();
        File file = getPlayerFile(uuid);
        if (!file.exists()) return homes;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        var section = cfg.getConfigurationSection("homes");
        if (section == null) return homes;

        for (String key : section.getKeys(false)) {
            String world     = section.getString(key + ".world");
            double x         = section.getDouble(key + ".x");
            double y         = section.getDouble(key + ".y");
            double z         = section.getDouble(key + ".z");
            float  yaw       = (float) section.getDouble(key + ".yaw");
            float  pitch     = (float) section.getDouble(key + ".pitch");
            long   createdAt = section.getLong(key + ".created-at", System.currentTimeMillis());
            String name      = section.getString(key + ".name", key);

            homes.put(key, new Home(uuid, name, world, x, y, z, yaw, pitch, createdAt));
        }

        return homes;
    }

    private void savePlayer(UUID uuid) {
        Map<String, Home> homes = homeCache.getOrDefault(uuid, Collections.emptyMap());
        File file = getPlayerFile(uuid);

        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<String, Home> entry : homes.entrySet()) {
            Home h = entry.getValue();
            String path = "homes." + entry.getKey();
            cfg.set(path + ".name",       h.getName());
            cfg.set(path + ".world",      h.getWorld());
            cfg.set(path + ".x",          h.getX());
            cfg.set(path + ".y",          h.getY());
            cfg.set(path + ".z",          h.getZ());
            cfg.set(path + ".yaw",        (double) h.getYaw());
            cfg.set(path + ".pitch",      (double) h.getPitch());
            cfg.set(path + ".created-at", h.getCreatedAt());
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save homes for " + uuid + ": " + e.getMessage());
        }
    }

    /** Saves all cached players to disk. Called on plugin disable. */
    public void saveAll() {
        for (UUID uuid : homeCache.keySet()) {
            savePlayer(uuid);
        }
        plugin.getLogger().info("All home data saved (" + homeCache.size() + " players).");
    }

    private File getPlayerFile(UUID uuid) {
        return new File(homesDir, uuid.toString() + ".yml");
    }

    /** Clears the in-memory cache for a player (called on player quit). */
    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        homeCache.remove(uuid);
    }
}
