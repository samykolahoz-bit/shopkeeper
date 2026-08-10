package me.samy.shopkeeper.gui;

import me.samy.shopkeeper.ShopKeeperPlugin;
import me.samy.shopkeeper.economy.EconomyManager;
import me.samy.shopkeeper.shop.Shop;
import me.samy.shopkeeper.shop.ShopItem;
import me.samy.shopkeeper.shop.ShopManager;
import me.samy.shopkeeper.util.ItemUtil;
import me.samy.shopkeeper.util.MessageUtil;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PurchaseGUI {
    private final Shop shop;
    private final ShopItem item;
    private final ShopKeeperPlugin plugin;
    private final int initialQty;
    private Inventory inv;
    private int qty;

    public PurchaseGUI(ShopKeeperPlugin plugin, Shop shop, ShopItem item, int initialQty) {
        this.plugin = plugin;
        this.shop = shop;
        this.item = item;
        this.initialQty = Math.max(1, initialQty);
        this.qty = this.initialQty;
        build();
    }

    private void build() {
        inv = Bukkit.createInventory(null, 9, ChatColor.GRAY + "Purchase: " + ItemUtil.getDisplayName(item.getItem()));
        // controls: -16 -1 qty +1 +16 MAX, buy, cancel, preview
        inv.setItem(1, ItemUtil.makeControlItem(Material.REDSTONE, ChatColor.RED + "-16"));
        inv.setItem(2, ItemUtil.makeControlItem(Material.REDSTONE, ChatColor.RED + "-1"));
        inv.setItem(3, ItemUtil.makeControlItem(Material.PAPER, ChatColor.GOLD + "Quantity: " + qty));
        inv.setItem(4, ItemUtil.makeControlItem(Material.EMERALD, ChatColor.GREEN + "Buy"));
        inv.setItem(5, ItemUtil.makeControlItem(Material.BARRIER, ChatColor.RED + "Cancel"));
        inv.setItem(6, ItemUtil.makeControlItem(Material.GREEN_CONCRETE, ChatColor.GREEN + "+1"));
        inv.setItem(7, ItemUtil.makeControlItem(Material.GREEN_CONCRETE, ChatColor.GREEN + "+16"));
        inv.setItem(0, item.getItem().clone());
    }

    public void open(Player p) {
        p.openInventory(inv);
        p.getServer().getPluginManager().registerEvents(new PurchaseHandler(), plugin);
    }

    private class PurchaseHandler implements org.bukkit.event.Listener {
        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory() != inv) return;
            org.bukkit.event.HandlerList.unregisterAll(this);
        }

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (e.getInventory() != inv) return;
            e.setCancelled(true);
            if (!(e.getWhoClicked() instanceof Player)) return;
            Player p = (Player) e.getWhoClicked();
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null) return;
            String name = ItemUtil.getDisplayName(clicked);
            if (name.contains("-16")) { qty = Math.max(1, qty - 16); rebuild(); }
            else if (name.contains("-1")) { qty = Math.max(1, qty - 1); rebuild(); }
            else if (name.contains("+1")) { qty = qty + 1; rebuild(); }
            else if (name.contains("+16")) { qty = qty + 16; rebuild(); }
            else if (name.contains("MAX")) { qty = item.getMaxPurchase() > 0 ? item.getMaxPurchase() : 64; rebuild(); }
            else if (name.contains("Buy")) {
                attemptPurchase(p);
            } else if (name.contains("Cancel")) {
                p.closeInventory();
            }
        }

        private void rebuild() {
            inv.setItem(3, ItemUtil.makeControlItem(org.bukkit.Material.PAPER, ChatColor.GOLD + "Quantity: " + qty));
        }

        private void attemptPurchase(Player p) {
            if (!plugin.getEconomyManager().isEnabled()) {
                p.sendMessage(ChatColor.RED + "Economy not available.");
                return;
            }
            if (item.getBuyPrice() < 0) {
                p.sendMessage(ChatColor.RED + "Item has no buy price.");
                return;
            }
            if (item.getPermission() != null && !p.hasPermission(item.getPermission())) {
                p.sendMessage(ChatColor.RED + "You don't have permission to buy this.");
                return;
            }
            if (item.getMaxPurchase() > 0 && qty > item.getMaxPurchase()) {
                p.sendMessage(ChatColor.RED + "Purchase limit exceeded.");
                return;
            }
            if (item.getStock() >= 0 && qty > item.getStock()) {
                p.sendMessage(ChatColor.RED + "Not enough stock.");
                return;
            }
            double total = item.getBuyPrice() * qty;
            EconomyManager eco = plugin.getEconomyManager();
            double balance = eco.getEconomy().getBalance(p);
            if (balance < total) {
                p.sendMessage(ChatColor.RED + "You don't have enough money.");
                return;
            }
            // inventory space check
            ItemStack stackToGive = item.getItem().clone();
            stackToGive.setAmount(Math.min(stackToGive.getMaxStackSize(), qty));
            if (!p.getInventory().addItem(stackToGive.clone()).isEmpty()) {
                p.sendMessage(ChatColor.RED + "Your inventory is full.");
                return;
            }
            // perform withdraw & give safely on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                EconomyResponse r = eco.getEconomy().withdrawPlayer(p, total);
                if (!r.transactionSuccess()) {
                    p.sendMessage(ChatColor.RED + "Transaction failed: " + r.errorMessage);
                    return;
                }
                // give rest of items if qty > maxStack: iterative give
                int remaining = qty;
                while (remaining > 0) {
                    ItemStack toGive = item.getItem().clone();
                    int give = Math.min(toGive.getMaxStackSize(), remaining);
                    toGive.setAmount(give);
                    p.getInventory().addItem(toGive);
                    remaining -= give;
                }
                // update stock
                if (item.getStock() >= 0) {
                    item.setStock(item.getStock() - qty);
                    plugin.getShopManager().saveShopAsync(shop);
                }
                plugin.getServer().getPluginManager().callEvent(new me.samy.shopkeeper.events.ShopPurchaseEvent(p, shop, item, qty, total));
                p.sendMessage(ChatColor.GREEN + "[Shop] You purchased " + qty + "x " + ItemUtil.getDisplayName(item.getItem()) + " for $" + total);
                p.closeInventory();
            });
        }
    }
}