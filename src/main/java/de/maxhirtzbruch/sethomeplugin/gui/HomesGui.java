package de.maxhirtzbruch.sethomeplugin.gui;

import de.maxhirtzbruch.sethomeplugin.SetHomePlugin;
import de.maxhirtzbruch.sethomeplugin.models.Home;
import de.maxhirtzbruch.sethomeplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The paginated Homes GUI (InventoryHolder pattern).
 *
 * Layout (6 rows = 54 slots):
 *   Rows 0-4  → home items  (45 slots)
 *   Row 5     → [PREV] [filler…] [CLOSE] [filler…] [NEXT]
 *               slot 45            slot 49  slot 53
 */
public class HomesGui implements InventoryHolder {

    // Slot indices in the navigation bar (last row)
    public static final int SLOT_PREV  = 45;
    public static final int SLOT_CLOSE = 49;
    public static final int SLOT_NEXT  = 53;

    private final SetHomePlugin plugin;
    private final Inventory inventory;
    private final int page;
    private final int totalPages;
    private final List<Home> homes;

    public HomesGui(SetHomePlugin plugin, List<Home> homes, int page) {
        this.plugin = plugin;
        this.homes  = homes;
        this.page   = page;

        int rows        = plugin.getConfigManager().getGuiRows();
        int contentSlots = (rows - 1) * 9;   // bottom row reserved for nav
        this.totalPages = Math.max(1, (int) Math.ceil((double) homes.size() / contentSlots));

        int maxHomes = plugin.getLuckPermsManager().isHooked()
                ? -1   // will be resolved per-player in command
                : plugin.getConfigManager().getDefaultMaxHomes();

        String title = plugin.getConfigManager().getMessageRaw(
                "gui-title",
                "current", String.valueOf(homes.size()),
                "max",     maxHomes < 0 ? "?" : String.valueOf(maxHomes)
        );

        this.inventory = Bukkit.createInventory(this, rows * 9, title);
        populate(contentSlots);
    }

    /** Overload with known max for proper title. */
    public HomesGui(SetHomePlugin plugin, List<Home> homes, int page, int maxHomes) {
        this.plugin = plugin;
        this.homes  = homes;
        this.page   = page;

        int rows         = plugin.getConfigManager().getGuiRows();
        int contentSlots = (rows - 1) * 9;
        this.totalPages  = Math.max(1, (int) Math.ceil((double) homes.size() / contentSlots));

        String title = plugin.getConfigManager().getMessageRaw(
                "gui-title",
                "current", String.valueOf(homes.size()),
                "max",     maxHomes == Integer.MAX_VALUE ? "∞" : String.valueOf(maxHomes)
        );

        this.inventory = Bukkit.createInventory(this, rows * 9, title);
        populate(contentSlots);
    }

    // ──────────────────────────────────────────────────────────────────────────

    private void populate(int contentSlots) {
        int startIndex = page * contentSlots;
        int endIndex   = Math.min(startIndex + contentSlots, homes.size());

        // Fill content area with homes
        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            inventory.setItem(slot, buildHomeItem(homes.get(i)));
        }

        // Fill remaining content slots with glass pane
        for (int slot = endIndex - startIndex; slot < contentSlots; slot++) {
            inventory.setItem(slot, buildFiller());
        }

        // Navigation row
        int navStart = contentSlots;
        for (int slot = navStart; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, buildFiller());
        }

        // Prev page button
        if (page > 0) {
            inventory.setItem(SLOT_PREV,
                    new ItemBuilder(plugin.getConfigManager().getPrevPageItem())
                            .name(plugin.getConfigManager().getMessageRaw("gui-prev-page"))
                            .hideFlags()
                            .build());
        }

        // Next page button
        if (page < totalPages - 1) {
            inventory.setItem(SLOT_NEXT,
                    new ItemBuilder(plugin.getConfigManager().getNextPageItem())
                            .name(plugin.getConfigManager().getMessageRaw("gui-next-page"))
                            .hideFlags()
                            .build());
        }

        // Close button
        inventory.setItem(SLOT_CLOSE,
                new ItemBuilder(plugin.getConfigManager().getCloseItem())
                        .name(plugin.getConfigManager().getMessageRaw("gui-close"))
                        .hideFlags()
                        .build());
    }

    private org.bukkit.inventory.ItemStack buildHomeItem(Home home) {
        List<String> lore = plugin.getConfigManager().getMessageList(
                "gui-home-lore",
                "world", home.getWorld(),
                "x",     home.getFormattedX(),
                "y",     home.getFormattedY(),
                "z",     home.getFormattedZ()
        );

        return new ItemBuilder(plugin.getConfigManager().getHomeItem())
                .name(de.maxhirtzbruch.sethomeplugin.managers.ConfigManager.colorize("&e⌂ &f" + home.getName()))
                .lore(lore)
                .hideFlags()
                .build();
    }

    private org.bukkit.inventory.ItemStack buildFiller() {
        return new ItemBuilder(plugin.getConfigManager().getFillerItem())
                .name(plugin.getConfigManager().getMessageRaw("gui-filler-name"))
                .hideFlags()
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getPage()        { return page; }
    public int getTotalPages()  { return totalPages; }
    public List<Home> getHomes(){ return homes; }

    /**
     * Returns the Home associated with the clicked content slot,
     * or null if no home is at that slot.
     */
    public Home getHomeAt(int slot) {
        int rows         = plugin.getConfigManager().getGuiRows();
        int contentSlots = (rows - 1) * 9;
        if (slot < 0 || slot >= contentSlots) return null;

        int index = page * contentSlots + slot;
        if (index >= homes.size()) return null;
        return homes.get(index);
    }
}
