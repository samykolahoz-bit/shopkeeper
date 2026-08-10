package me.samy.shopkeeper.gui;

import me.samy.shopkeeper.ShopKeeperPlugin;
import me.samy.shopkeeper.shop.Shop;
import me.samy.shopkeeper.shop.ShopItem;
import me.samy.shopkeeper.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {
    private final Shop shop;
    private final ShopKeeperPlugin plugin;
    private Inventory inv;

    public ShopGUI(ShopKeeperPlugin plugin, Shop shop) {
        this.plugin = plugin;
        this.shop = shop;
        build();
    }

    public static void register(ShopKeeperPlugin plugin) {
        // placeholder to ensure class load; listeners are registered elsewhere
    }

    private void build() {
        int rows = Math.max(1, Math.min(6, shop.getGui().rows));
        inv = Bukkit.createInventory(null, rows * 9, ChatColor.translateAlternateColorCodes('&', shop.getGui().title));
        // fill border with gray panes
        ItemStack border = ItemUtil.makeBorderPane();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, null);
        }
        int slot = 0;
        for (ShopItem si : shop.getItems()) {
            ItemStack display = si.getItem().clone();
            ItemUtil.addShopLore(display, si);
            inv.setItem(slot, display);
            slot++;
            if (slot >= inv.getSize()) break;
        }
    }

    public void open(Player p) {
        p.openInventory(inv);
        // register listener for clicks
        p.getServer().getPluginManager().registerEvents(new InventoryClickHandler(), plugin);
    }

    private class InventoryClickHandler implements org.bukkit.event.Listener {
        @EventHandler
        public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent e) {
            // unregister this listener
            org.bukkit.event.HandlerList.unregisterAll(this);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getInventory() != inv) return;
            e.setCancelled(true);
            if (!(e.getWhoClicked() instanceof Player)) return;
            Player p = (Player) e.getWhoClicked();
            int slot = e.getRawSlot();
            if (slot < 0 || slot >= inv.getSize()) return;
            ItemStack clicked = inv.getItem(slot);
            if (clicked == null) return;
            // find matching shopitem
            ShopItem matched = null;
            for (ShopItem si : shop.getItems()) {
                if (ItemUtil.isSimilar(clicked, si.getItem())) {
                    matched = si;
                    break;
                }
            }
            if (matched == null) return;
            // Left click => Buy 1, Shift-left => Buy 16, Right click => Sell 1
            ClickType ct = e.getClick();
            int qty = 1;
            if (ct == ClickType.SHIFT_LEFT) qty = 16;
            if (ct == ClickType.SHIFT_RIGHT) qty = 16;
            if (ct == ClickType.NUMBER_KEY) return;
            if (ct == ClickType.RIGHT) {
                // open sell interface if enabled
                if (!matched.isSellEnabled()) {
                    p.sendMessage(ChatColor.RED + "Selling disabled for this item.");
                    return;
                }
                new SellGUI(plugin, shop, matched).open(p);
            } else {
                if (!matched.isBuyEnabled()) {
                    p.sendMessage(ChatColor.RED + "Buying disabled for this item.");
                    return;
                }
                new PurchaseGUI(plugin, shop, matched, qty).open(p);
            }
        }
    }
}