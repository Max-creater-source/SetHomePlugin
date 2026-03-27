package de.maxhirtzbruch.sethomeplugin.commands;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import de.maxhirtzbruch.sethomeplugin.models.Home;
import de.maxhirtzbruch.sethomeplugin.utils.SoundUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * /home <n>
 */
public class HomeCommand implements CommandExecutor, TabCompleter {

    private final SetHomePlugin plugin;

    public HomeCommand(SetHomePlugin plugin) {
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
            player.sendMessage("§cUsage: /home <n>");
            return true;
        }

        String name = args[0];
        Home home = plugin.getHomeManager().getHome(player.getUniqueId(), name);

        if (home == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("home-not-found", "name", name));
            SoundUtil.playError(plugin, player);
            return true;
        }

        plugin.getTeleportManager().startTeleport(player, home);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            List<String> names = new ArrayList<>();
            plugin.getHomeManager().getHomeList(player.getUniqueId())
                    .forEach(h -> {
                        if (h.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                            names.add(h.getName());
                        }
                    });
            return names;
        }
        return List.of();
    }
}
