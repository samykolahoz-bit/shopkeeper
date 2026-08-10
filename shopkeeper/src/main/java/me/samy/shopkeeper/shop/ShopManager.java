package me.samy.shopkeeper.shop;

import me.samy.shopkeeper.ShopKeeperPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopManager {
    private final ShopKeeperPlugin plugin;
    private final Map<String, Shop> shops = new ConcurrentHashMap<>();
    private final File shopsDir;

    public ShopManager(ShopKeeperPlugin plugin) {
        this.plugin = plugin;
        this.shopsDir = new File(plugin.getDataFolder(), "shops");
        if (!shopsDir.exists()) shopsDir.mkdirs();
    }

    public void loadAllShops() {
        shops.clear();
        File[] files = shopsDir.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;
        for (File f : files) {
            Shop s = Shop.loadFromFile(f);
            if (s != null) shops.put(s.getId(), s);
        }
        plugin.getLogger().info("Loaded " + shops.size() + " shops.");
    }

    public Collection<Shop> getShops() { return shops.values(); }
    public Optional<Shop> getShop(String id) { return Optional.ofNullable(shops.get(id)); }

    public Shop createShop(String id, String name) {
        Shop s = new Shop(id);
        s.setName(name);
        shops.put(id, s);
        saveShopAsync(s);
        return s;
    }

    public void deleteShop(String id) {
        Shop s = shops.remove(id);
        if (s != null) {
            File f = new File(shopsDir, id + ".yml");
            if (f.exists()) f.delete();
        }
    }

    public void saveShopAsync(Shop shop) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File f = new File(shopsDir, shop.getId() + ".yml");
            shop.saveToFile(f);
        });
    }

    public void saveAllShopsSync() {
        for (Shop s : shops.values()) {
            File f = new File(shopsDir, s.getId() + ".yml");
            s.saveToFile(f);
        }
    }
}