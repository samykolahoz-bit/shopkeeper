package me.samy.shopkeeper.entity;

import me.samy.shopkeeper.ShopKeeperPlugin;
import me.samy.shopkeeper.shop.Shop;
import me.samy.shopkeeper.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShopEntityManager {
    private final ShopKeeperPlugin plugin;
    private final ShopManager shopManager;
    private final NamespacedKey key;

    public ShopEntityManager(ShopKeeperPlugin plugin, ShopManager shopManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.key = new NamespacedKey(plugin, "shopkeeper-id");
    }

    public void ensureAllEntities() {
        // Recreate missing entities asynchronously to avoid long blocking operations
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Shop s : shopManager.getShops()) {
                if (s.getLocation() == null) continue;
                boolean found = false;
                for (Entity e : s.getLocation().getWorld().getEntities()) {
                    if (! (e instanceof LivingEntity)) continue;
                    LivingEntity le = (LivingEntity) e;
                    if (le.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                        String id = le.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                        if (id != null && id.equals(s.getId())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) spawnShopkeeper(s);
            }
        }, 20L);
    }

    public LivingEntity spawnShopkeeper(Shop shop) {
        Location loc = shop.getLocation();
        if (loc == null) return null;
        try {
            LivingEntity ent = (LivingEntity) loc.getWorld().spawnEntity(loc, shop.getMobType());
            ent.setInvulnerable(shop.getSettings().invulnerable);
            ent.setCustomNameVisible(true);
            ent.setCustomName(org.bukkit.ChatColor.translateAlternateColorCodes('&', shop.getDisplayName() == null ? shop.getName() : shop.getDisplayName()));
            ent.setRemoveWhenFarAway(!shop.getSettings().preventDespawn);
            ent.setAI(!shop.getSettings().preventMovement); // if preventMovement true, disable AI
            // store shop id
            ent.getPersistentDataContainer().set(key, PersistentDataType.STRING, shop.getId());
            // additional protections
            ent.setCollidable(!shop.getSettings().preventPush);
            return ent;
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to spawn shopkeeper for shop: " + shop.getId() + " reason: " + ex.getMessage());
            return null;
        }
    }

    public String getShopIdFromEntity(Entity e) {
        if (!(e instanceof LivingEntity)) return null;
        LivingEntity le = (LivingEntity) e;
        if (!le.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return null;
        return le.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}