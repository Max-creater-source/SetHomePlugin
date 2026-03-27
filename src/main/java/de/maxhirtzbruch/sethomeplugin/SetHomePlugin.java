package de.maxhirtzbruch.sethomeplugin;

import de.maxhirtzbruch.sethomeplugin.commands.*;
import de.maxhirtzbruch.sethomeplugin.listeners.GuiListener;
import de.maxhirtzbruch.sethomeplugin.listeners.PlayerMoveListener;
import de.maxhirtzbruch.sethomeplugin.managers.ConfigManager;
import de.maxhirtzbruch.sethomeplugin.managers.HomeManager;
import de.maxhirtzbruch.sethomeplugin.managers.LuckPermsManager;
import de.maxhirtzbruch.sethomeplugin.managers.TeleportManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SetHomePlugin – Main Plugin Class
 *
 * @author Maximilian Hirtzbruch
 * @version 1.0
 * Compatible: Bukkit / Spigot / Paper / Folia / Purpur  (1.21 – 1.21.x)
 */
public final class SetHomePlugin extends JavaPlugin {

    private static SetHomePlugin instance;

    private ConfigManager configManager;
    private HomeManager homeManager;
    private LuckPermsManager luckPermsManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        instance = this;

        printBanner();

        // ── Managers ────────────────────────────────────────
        this.configManager   = new ConfigManager(this);
        this.luckPermsManager = new LuckPermsManager(this);
        this.homeManager     = new HomeManager(this);
        this.teleportManager = new TeleportManager(this);

        // ── Commands ─────────────────────────────────────────
        registerCommands();

        // ── Listeners ────────────────────────────────────────
        registerListeners();

        getLogger().info("SetHomePlugin v" + getDescription().getVersion()
                + " by " + getDescription().getAuthors().get(0) + " has been enabled!");
    }

    @Override
    public void onDisable() {
        if (homeManager != null) {
            homeManager.saveAll();
        }
        if (teleportManager != null) {
            teleportManager.cancelAll();
        }
        getLogger().info("SetHomePlugin has been disabled. All homes saved.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Registration helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void registerCommands() {
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("sethome").setTabCompleter(new SetHomeCommand(this));

        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("home").setTabCompleter(new HomeCommand(this));

        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("delhome").setTabCompleter(new DelHomeCommand(this));

        getCommand("homes").setExecutor(new HomesCommand(this));

        getCommand("sethomereload").setExecutor(new ReloadCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Static accessor
    // ──────────────────────────────────────────────────────────────────────────

    public static SetHomePlugin getInstance() {
        return instance;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Getters
    // ──────────────────────────────────────────────────────────────────────────

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public LuckPermsManager getLuckPermsManager() {
        return luckPermsManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Pretty banner
    // ──────────────────────────────────────────────────────────────────────────

    private void printBanner() {
        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║  ⌂  SetHomePlugin  v1.0              ║");
        getLogger().info("║  Author: Maximilian Hirtzbruch       ║");
        getLogger().info("║  MC: 1.21 – 1.21.x                  ║");
        getLogger().info("╚══════════════════════════════════════╝");
    }
}
