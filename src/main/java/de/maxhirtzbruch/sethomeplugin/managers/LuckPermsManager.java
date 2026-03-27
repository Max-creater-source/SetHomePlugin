package de.maxhirtzbruch.sethomeplugin.managers;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Map;
import java.util.TreeMap;

/**
 * Integrates with LuckPerms to determine how many homes a player may have.
 *
 * Resolution order:
 *  1. Permission node   sethome.homes.unlimited  → Integer.MAX_VALUE
 *  2. Permission node   sethome.homes.<n>         → n  (highest wins)
 *  3. LuckPerms primary group matched in config rank-homes map
 *  4. config default-max-homes
 */
public class LuckPermsManager {

    private final SetHomePlugin plugin;
    private LuckPerms luckPerms;

    public LuckPermsManager(SetHomePlugin plugin) {
        this.plugin = plugin;
        hookLuckPerms();
    }

    private void hookLuckPerms() {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            RegisteredServiceProvider<LuckPerms> provider =
                    plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                this.luckPerms = provider.getProvider();
                plugin.getLogger().info("LuckPerms hooked successfully.");
            }
        } else {
            plugin.getLogger().warning("LuckPerms not found. Falling back to permission nodes and config defaults.");
        }
    }

    /**
     * Returns the maximum number of homes allowed for this player.
     */
    public int getMaxHomes(Player player) {

        // 1. Unlimited permission
        if (player.hasPermission("sethome.homes.unlimited")) {
            return Integer.MAX_VALUE;
        }

        // 2. Numeric permission nodes (sethome.homes.N)
        int permissionMax = -1;
        for (int n : new int[]{1, 2, 3, 5, 10, 15, 20, 50, 100}) {
            if (player.hasPermission("sethome.homes." + n)) {
                permissionMax = Math.max(permissionMax, n);
            }
        }
        if (permissionMax >= 0) return permissionMax;

        // 3. LuckPerms rank map from config
        if (luckPerms != null) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                // Collect all group names the player has (including inherited)
                Map<String, Integer> rankHomes = new TreeMap<>(
                        String.CASE_INSENSITIVE_ORDER);
                rankHomes.putAll(plugin.getConfigManager().getRankHomes());

                // Walk inherited groups, pick highest configured value
                int best = -1;
                for (Node node : user.getNodes()) {
                    if (node.getKey().startsWith("group.")) {
                        String groupName = node.getKey().substring(6);
                        if (rankHomes.containsKey(groupName)) {
                            best = Math.max(best, rankHomes.get(groupName));
                        }
                    }
                }
                if (best >= 0) return best;

                // Primary group fallback
                String primaryGroup = user.getPrimaryGroup();
                if (rankHomes.containsKey(primaryGroup)) {
                    return rankHomes.get(primaryGroup);
                }
            }
        }

        // 4. Config default
        return plugin.getConfigManager().getDefaultMaxHomes();
    }

    public boolean isHooked() {
        return luckPerms != null;
    }
}
