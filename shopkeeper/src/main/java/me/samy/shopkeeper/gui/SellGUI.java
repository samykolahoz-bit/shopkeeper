package me.samy.shopkeeper.gui;

import me.samy.shopkeeper.ShopKeeperPlugin;
import me.samy.shopkeeper.economy.EconomyManager;
import me.samy.shopkeeper.shop.Shop;
import me.samy.shopkeeper.shop.ShopItem;
import me.samy.shopkeeper.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellGUI {

    private final Shop shop;
    private final ShopItem item;
    private final ShopKeeperPlugin plugin;

    private Inventory inv;

    public SellGUI(
            ShopKeeperPlugin plugin,
            Shop shop,
            ShopItem item
    ) {
        this.plugin = plugin;
        this.shop = shop;
        this.item = item;
        build();
    }

    private void build() {

        inv = Bukkit.createInventory(
                null,
                9,
                ChatColor.GRAY
                        + "Sell: "
                        + ItemUtil.getDisplayName(item.getItem())
        );

        inv.setItem(
                3,
                ItemUtil.makeControlItem(
                        Material.PAPER,
                        ChatColor.GOLD
                                + "Sell All matching"
                )
        );

        inv.setItem(
                4,
                ItemUtil.makeControlItem(
                        Material.EMERALD,
                        ChatColor.GREEN
                                + "Sell"
                )
        );

        inv.setItem(
                5,
                ItemUtil.makeControlItem(
                        Material.BARRIER,
                        ChatColor.RED
                                + "Cancel"
                )
        );
    }

    public void open(Player p) {

        p.openInventory(inv);

        p.getServer()
                .getPluginManager()
                .registerEvents(
                        new SellHandler(),
                        plugin
                );
    }

    private class SellHandler implements Listener {

        @EventHandler
        public void onInventoryClose(
                InventoryCloseEvent e
        ) {

            if (e.getInventory() != inv) {
                return;
            }

            HandlerList.unregisterAll(this);
        }

        @EventHandler
        public void onClick(
                InventoryClickEvent e
        ) {

            if (e.getInventory() != inv) {
                return;
            }

            e.setCancelled(true);

            if (!(e.getWhoClicked() instanceof Player)) {
                return;
            }

            Player p =
                    (Player) e.getWhoClicked();

            ItemStack clicked =
                    e.getCurrentItem();

            if (clicked == null) {
                return;
            }

            String name =
                    ItemUtil.getDisplayName(clicked);

            if (name.contains("Sell All")) {

                attemptSellAll(p);

            } else if (name.equals(
                    ChatColor.GREEN + "Sell"
            ) || name.contains("Sell")) {

                attemptSell(p);

            } else if (name.contains("Cancel")) {

                p.closeInventory();
            }
        }

        private void attemptSellAll(Player p) {

            int count = 0;

            for (ItemStack is :
                    p.getInventory().getContents()) {

                if (is == null ||
                        is.getType().isAir()) {
                    continue;
                }

                if (ItemUtil.isSimilar(
                        is,
                        item.getItem()
                )) {

                    count += is.getAmount();
                }
            }

            if (count <= 0) {

                p.sendMessage(
                        ChatColor.RED
                                + "You have none of that item."
                );

                return;
            }

            performSell(
                    p,
                    count
            );
        }

        private void attemptSell(Player p) {

            ItemStack hand =
                    p.getInventory()
                            .getItemInMainHand();

            if (hand == null ||
                    hand.getType().isAir()) {

                p.sendMessage(
                        ChatColor.RED
                                + "Hold the item to sell in your hand."
                );

                return;
            }

            if (!ItemUtil.isSimilar(
                    hand,
                    item.getItem()
            )) {

                p.sendMessage(
                        ChatColor.RED
                                + "Item in hand does not match the shop item."
                );

                return;
            }

            performSell(
                    p,
                    hand.getAmount()
            );
        }

        private void performSell(
                Player p,
                int amount
        ) {

            if (!plugin.getEconomyManager()
                    .isEnabled()) {

                p.sendMessage(
                        ChatColor.RED
                                + "Economy not available."
                );

                return;
            }

            if (item.getSellPrice() < 0) {

                p.sendMessage(
                        ChatColor.RED
                                + "Item has no sell price."
                );

                return;
            }

            if (amount <= 0) {
                return;
            }

            int removed = 0;

            for (int i = 0;
                 i < p.getInventory().getSize();
                 i++) {

                ItemStack is =
                        p.getInventory().getItem(i);

                if (is == null ||
                        is.getType().isAir()) {
                    continue;
                }

                if (!ItemUtil.isSimilar(
                        is,
                        item.getItem()
                )) {
                    continue;
                }

                int remaining =
                        amount - removed;

                int take =
                        Math.min(
                                is.getAmount(),
                                remaining
                        );

                is.setAmount(
                        is.getAmount() - take
                );

                if (is.getAmount() <= 0) {

                    p.getInventory()
                            .setItem(i, null);
                }

                removed += take;

                if (removed >= amount) {
                    break;
                }
            }

            if (removed <= 0) {

                p.sendMessage(
                        ChatColor.RED
                                + "You don't have that item."
                );

                return;
            }

            final int finalRemoved =
                    removed;

            final double total =
                    item.getSellPrice()
                            * finalRemoved;

            plugin.getServer()
                    .getScheduler()
                    .runTask(
                            plugin,
                            () -> {

                                EconomyManager eco =
                                        plugin.getEconomyManager();

                                if (!eco.isEnabled()) {

                                    p.sendMessage(
                                            ChatColor.RED
                                                    + "Economy not available."
                                    );

                                    return;
                                }

                                eco.getEconomy()
                                        .depositPlayer(
                                                p,
                                                total
                                        );

                                if (item.getStock() >= 0) {

                                    item.setStock(
                                            item.getStock()
                                                    + finalRemoved
                                    );

                                    plugin.getShopManager()
                                            .saveShopAsync(
                                                    shop
                                            );
                                }

                                p.sendMessage(
                                        ChatColor.GREEN
                                                + "[Shop] You sold "
                                                + finalRemoved
                                                + "x "
                                                + ItemUtil.getDisplayName(
                                                        item.getItem()
                                                )
                                                + " for $"
                                                + total
                                );

                                p.closeInventory();
                            }
                    );
        }
    }
}
