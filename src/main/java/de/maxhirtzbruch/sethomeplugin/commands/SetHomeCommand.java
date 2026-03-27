package de.maxhirtzbruch.sethomeplugin.commands;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import de.maxhirtzbruch.sethomeplugin.utils.HomeValidator;
import de.maxhirtzbruch.sethomeplugin.utils.SoundUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * /sethome <name>
 */
public class SetHomeCommand implements CommandExecutor, TabCompleter {

    private final SetHomePlugin plugin;

    public SetHomeCommand(SetHomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("sethome.use")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            SoundUtil.playError(plugin, player);
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /sethome <name>");
            return true;
        }

        String name = args[0];

        // Validate name
        HomeValidator.ValidationResult result = HomeValidator.validate(name);
        if (result == HomeValidator.ValidationResult.TOO_LONG) {
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "home-name-too-long",
                    "max", String.valueOf(plugin.getConfigManager().getMaxHomeNameLength())
            ));
            SoundUtil.playError(plugin, player);
            return true;
        }
        if (result == HomeValidator.ValidationResult.INVALID_CHARS) {
            player.sendMessage(plugin.getConfigManager().getMessage("home-name-invalid"));
            SoundUtil.playError(plugin, player);
            return true;
        }

        // Check if name already exists
        if (plugin.getHomeManager().getHome(player.getUniqueId(), name) != null) {
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "home-already-exists", "name", name));
            SoundUtil.playError(plugin, player);
            return true;
        }

        // Try to set
        boolean success = plugin.getHomeManager().setHome(player, name);
        if (!success) {
            int max     = plugin.getLuckPermsManager().getMaxHomes(player);
            int current = plugin.getHomeManager().getHomeCount(player.getUniqueId());
            player.sendMessage(plugin.getConfigManager().getMessage(
                    "home-limit-reached",
                    "current", String.valueOf(current),
                    "max",     max == Integer.MAX_VALUE ? "∞" : String.valueOf(max)
            ));
            SoundUtil.playError(plugin, player);
            return true;
        }

        player.sendMessage(plugin.getConfigManager().getMessage("home-set", "name", name));
        SoundUtil.playSetHome(plugin, player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("<name>");
        }
        return List.of();
    }
}
