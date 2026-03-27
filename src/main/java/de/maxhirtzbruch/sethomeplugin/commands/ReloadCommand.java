package de.maxhirtzbruch.sethomeplugin.commands;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * /sethomereload  –  Reloads the plugin configuration.
 */
public class ReloadCommand implements CommandExecutor {

    private final SetHomePlugin plugin;

    public ReloadCommand(SetHomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!sender.hasPermission("sethome.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        plugin.getConfigManager().load();
        sender.sendMessage(plugin.getConfigManager().getMessage("config-reloaded"));
        return true;
    }
}
