package de.maxhirtzbruch.sethomeplugin.utils;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Utility class for playing sounds safely.
 */
public final class SoundUtil {

    private SoundUtil() {}

    public static void playSound(SetHomePlugin plugin, Player player, String soundName) {
        if (!plugin.getConfigManager().isSoundsEnabled()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
            // Invalid sound in config – silently skip
        }
    }

    public static void playSetHome(SetHomePlugin plugin, Player player) {
        playSound(plugin, player, plugin.getConfigManager().getSetHomeSound());
    }

    public static void playDeleteHome(SetHomePlugin plugin, Player player) {
        playSound(plugin, player, plugin.getConfigManager().getDeleteHomeSound());
    }

    public static void playOpenGui(SetHomePlugin plugin, Player player) {
        playSound(plugin, player, plugin.getConfigManager().getOpenGuiSound());
    }

    public static void playError(SetHomePlugin plugin, Player player) {
        playSound(plugin, player, plugin.getConfigManager().getErrorSound());
    }
}
