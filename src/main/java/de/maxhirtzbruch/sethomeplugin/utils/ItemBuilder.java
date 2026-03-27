package de.maxhirtzbruch.sethomeplugin.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Fluent builder for ItemStacks used in GUI menus.
 */
public final class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta  meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(String materialName) {
        Material mat;
        try {
            mat = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            mat = Material.STONE;  // safe fallback
        }
        this.item = new ItemStack(mat);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String displayName) {
        if (meta != null) meta.setDisplayName(displayName);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (meta != null) meta.setLore(lore);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        if (meta != null) meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, amount));
        return this;
    }

    /** Hides all item flags (attributes, enchants etc.) for clean GUI items. */
    public ItemBuilder hideFlags() {
        if (meta != null) {
            for (org.bukkit.inventory.ItemFlag flag : org.bukkit.inventory.ItemFlag.values()) {
                meta.addItemFlags(flag);
            }
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) item.setItemMeta(meta);
        return item;
    }
}
