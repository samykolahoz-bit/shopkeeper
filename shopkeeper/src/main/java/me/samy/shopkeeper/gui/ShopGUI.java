package me.samy.shopkeeper.gui;

import me.samy.shopkeeper.ShopKeeperPlugin;
import me.samy.shopkeeper.shop.Shop;
import me.samy.shopkeeper.shop.ShopItem;
import me.samy.shopkeeper.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopGUI {

```
private final Shop shop;
private final ShopKeeperPlugin plugin;
private Inventory inv;

public ShopGUI(
        ShopKeeperPlugin plugin,
        Shop shop
) {
    this.plugin = plugin;
    this.shop = shop;
    build();
}

public static void register(
        ShopKeeperPlugin plugin
) {
    // Listeners are registered when the GUI is opened.
}

private void build() {

    int rows = Math.max(
            1,
            Math.min(
                    6,
                    shop.getGui().rows
            )
    );

    inv = Bukkit.createInventory(
            null,
            rows * 9,
            ChatColor.translateAlternateColorCodes(
                    '&',
                    shop.getGui().title
            )
    );

    for (int i = 0;
         i < inv.getSize();
         i++) {

        inv.setItem(
                i,
                null
        );
    }

    int slot = 0;

    for (ShopItem si :
            shop.getItems()) {

        if (slot >= inv.getSize()) {
            break;
        }

        ItemStack display =
                si.getItem().clone();

        ItemUtil.addShopLore(
                display,
                si
        );

        inv.setItem(
                slot,
                display
        );

        slot++;
    }
}

public void open(Player p) {

    p.openInventory(inv);

    p.getServer()
            .getPluginManager()
            .registerEvents(
                    new InventoryClickHandler(),
                    plugin
            );
}

private class InventoryClickHandler
        implements Listener {

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
    public void onInventoryClick(
            InventoryClickEvent e
    ) {

        if (e.getInventory() != inv) {
            return;
        }

        e.setCancelled(true);

        if (!(e.getWhoClicked()
                instanceof Player)) {

            return;
        }

        Player p =
                (Player) e.getWhoClicked();

        int slot =
                e.getRawSlot();

        if (slot < 0 ||
                slot >= inv.getSize()) {

            return;
        }

        ItemStack clicked =
                inv.getItem(slot);

        if (clicked == null ||
                clicked.getType().isAir()) {

            return;
        }

        ShopItem matched = null;

        for (ShopItem si :
                shop.getItems()) {

            if (ItemUtil.isSimilar(
                    clicked,
                    si.getItem()
            )) {

                matched = si;
                break;
            }
        }

        if (matched == null) {
            return;
        }

        ClickType clickType =
                e.getClick();

        int qty = 1;

        if (clickType ==
                ClickType.SHIFT_LEFT ||
            clickType ==
                ClickType.SHIFT_RIGHT) {

            qty = 16;
        }

        if (clickType ==
                ClickType.NUMBER_KEY) {

            return;
        }

        if (clickType ==
                ClickType.RIGHT) {

            if (!matched.isSellEnabled()) {

                p.sendMessage(
                        ChatColor.RED +
                                "Selling disabled for this item."
                );

                return;
            }

            new SellGUI(
                    plugin,
                    shop,
                    matched
            ).open(p);

            return;
        }

        if (clickType ==
                ClickType.LEFT ||
            clickType ==
                ClickType.SHIFT_LEFT ||
            clickType ==
                ClickType.SHIFT_RIGHT) {

            if (!matched.isBuyEnabled()) {

                p.sendMessage(
                        ChatColor.RED +
                                "Buying disabled for this item."
                );

                return;
            }

            new PurchaseGUI(
                    plugin,
                    shop,
                    matched,
                    qty
            ).open(p);
        }
    }
}
```

}
