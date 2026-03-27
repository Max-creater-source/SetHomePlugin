package de.maxhirtzbruch.sethomeplugin.managers;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles all configuration and message lookups.
 */
public class ConfigManager {

    private final SetHomePlugin plugin;
    private FileConfiguration config;
    private String language;

    public ConfigManager(SetHomePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config   = plugin.getConfig();
        this.language = config.getString("general.language", "en");
        plugin.getLogger().info("Language set to: " + language);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Message helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns a coloured message for the current language, with placeholder
     * substitution.  Falls back to "en" if the key is not found in the
     * configured language.
     *
     * @param key         message key (e.g. "home-set")
     * @param replacements alternating key/value pairs: "name", "myHome", ...
     */
    public String getMessage(String key, String... replacements) {
        String path = "messages." + language + "." + key;
        String msg  = config.getString(path);

        if (msg == null) {
            // fallback to English
            msg = config.getString("messages.en." + key, "&cMissing message: " + key);
        }

        // Apply placeholders
        if (replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                msg = msg.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }

        return colorize(getPrefix() + msg);
    }

    /**
     * Returns a message WITHOUT the plugin prefix.
     */
    public String getMessageRaw(String key, String... replacements) {
        String path = "messages." + language + "." + key;
        String msg  = config.getString(path);

        if (msg == null) {
            msg = config.getString("messages.en." + key, "&cMissing message: " + key);
        }

        if (replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                msg = msg.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }

        return colorize(msg);
    }

    /**
     * Returns a coloured String list (used for GUI lore).
     */
    public List<String> getMessageList(String key, String... replacements) {
        String path = "messages." + language + "." + key;
        List<String> lines = config.getStringList(path);

        if (lines.isEmpty()) {
            lines = config.getStringList("messages.en." + key);
        }

        return lines.stream().map(line -> {
            String l = line;
            if (replacements.length % 2 == 0) {
                for (int i = 0; i < replacements.length; i += 2) {
                    l = l.replace("{" + replacements[i] + "}", replacements[i + 1]);
                }
            }
            return colorize(l);
        }).collect(Collectors.toList());
    }

    public String getPrefix() {
        String path = "messages." + language + ".prefix";
        String prefix = config.getString(path, config.getString("messages.en.prefix", "&8[&bSetHome&8] "));
        return colorize(prefix);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Config value accessors
    // ──────────────────────────────────────────────────────────────────────────

    public int getDefaultMaxHomes() {
        return config.getInt("general.default-max-homes", 3);
    }

    public int getTeleportDelay() {
        return config.getInt("general.teleport-delay", 3);
    }

    public boolean isCancelOnMove() {
        return config.getBoolean("general.cancel-on-move", true);
    }

    public boolean isSoundsEnabled() {
        return config.getBoolean("general.sounds.enabled", true);
    }

    public String getTeleportSound() {
        return config.getString("general.sounds.teleport", "ENTITY_ENDERMAN_TELEPORT");
    }

    public String getSetHomeSound() {
        return config.getString("general.sounds.set-home", "BLOCK_ENCHANTMENT_TABLE_USE");
    }

    public String getDeleteHomeSound() {
        return config.getString("general.sounds.delete-home", "ENTITY_ITEM_BREAK");
    }

    public String getOpenGuiSound() {
        return config.getString("general.sounds.open-gui", "BLOCK_CHEST_OPEN");
    }

    public String getErrorSound() {
        return config.getString("general.sounds.error", "ENTITY_VILLAGER_NO");
    }

    public boolean isParticlesEnabled() {
        return config.getBoolean("general.particles.enabled", true);
    }

    public String getParticleType() {
        return config.getString("general.particles.type", "PORTAL");
    }

    public int getParticleCount() {
        return config.getInt("general.particles.count", 30);
    }

    /** Returns the rank→maxHomes map from config. */
    public Map<String, Integer> getRankHomes() {
        var section = config.getConfigurationSection("rank-homes");
        if (section == null) return Map.of();
        return section.getKeys(false).stream()
                .collect(Collectors.toMap(
                        k -> k,
                        k -> section.getInt(k, getDefaultMaxHomes())
                ));
    }

    // GUI
    public int getGuiRows() {
        return Math.max(1, Math.min(6, config.getInt("gui.rows", 6)));
    }

    public String getHomeItem() {
        return config.getString("gui.home-item", "LIME_BED");
    }

    public String getFillerItem() {
        return config.getString("gui.filler-item", "GRAY_STAINED_GLASS_PANE");
    }

    public String getPrevPageItem() {
        return config.getString("gui.prev-page-item", "ARROW");
    }

    public String getNextPageItem() {
        return config.getString("gui.next-page-item", "ARROW");
    }

    public String getCloseItem() {
        return config.getString("gui.close-item", "BARRIER");
    }

    public boolean isShowCoordinates() {
        return config.getBoolean("gui.show-coordinates", true);
    }

    public boolean isShowWorld() {
        return config.getBoolean("gui.show-world", true);
    }

    public boolean isConfirmEnabled() {
        return config.getBoolean("gui.confirm.enabled", true);
    }

    public String getConfirmItem() {
        return config.getString("gui.confirm.confirm-item", "LIME_CONCRETE");
    }

    public String getCancelItem() {
        return config.getString("gui.confirm.cancel-item", "RED_CONCRETE");
    }

    // Home name constraints
    public int getMaxHomeNameLength() { return 24; }

    // ──────────────────────────────────────────────────────────────────────────
    //  Utility
    // ──────────────────────────────────────────────────────────────────────────

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String getLanguage() {
        return language;
    }
}
