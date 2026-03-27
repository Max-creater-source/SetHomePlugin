package de.maxhirtzbruch.sethomeplugin.commands;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import de.maxhirtzbruch.sethomeplugin.gui.HomesGui;
import de.maxhirtzbruch.sethomeplugin.models.Home;
import de.maxhirtzbruch.sethomeplugin.utils.SoundUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * /homes  –  Opens the paginated Homes GUI.
 */
public class HomesCommand implements CommandExecutor {

    private final SetHomePlugin plugin;

    public HomesCommand(SetHomePlugin plugin) {
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

        List<Home> homes    = plugin.getHomeManager().getHomeList(player.getUniqueId());
        int        maxHomes = plugin.getLuckPermsManager().getMaxHomes(player);

        if (homes.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("homes-list-empty"));
            SoundUtil.playError(plugin, player);
            return true;
        }

        player.openInventory(new HomesGui(plugin, homes, 0, maxHomes).getInventory());
        SoundUtil.playOpenGui(plugin, player);
        return true;
    }
}
