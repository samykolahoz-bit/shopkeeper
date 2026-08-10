package me.samy.shopkeeper.listener;

import me.samy.shopkeeper.ShopKeeperPlugin;
import me.samy.shopkeeper.entity.ShopEntityManager;
import me.samy.shopkeeper.gui.ShopGUI;
import me.samy.shopkeeper.shop.Shop;
import me.samy.shopkeeper.shop.ShopManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class ShopListener implements Listener {
    private final ShopKeeperPlugin plugin;
    private final ShopManager shopManager;
    private final ShopEntityManager entityManager;

    public ShopListener(ShopKeeperPlugin plugin, ShopManager shopManager, ShopEntityManager entityManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.entityManager = entityManager;
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof org.bukkit.entity.LivingEntity)) return;
        String shopId = entityManager.getShopIdFromEntity(e.getRightClicked());
        if (shopId == null) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        shopManager.getShop(shopId).ifPresent(shop -> {
            ShopGUI gui = new ShopGUI(plugin, shop);
            gui.open(p);
        });
    }
}