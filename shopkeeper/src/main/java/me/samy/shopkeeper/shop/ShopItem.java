package me.samy.shopkeeper.shop;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.samy.shopkeeper.util.ItemUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopItem {
    private ItemStack item;
    private double buyPrice = -1;
    private double sellPrice = -1;
    private long stock = -1; // -1 means unlimited
    private int maxPurchase = -1; // -1 unlimited
    private boolean buyEnabled = true;
    private boolean sellEnabled = true;
    private String permission = null;

    public ShopItem(ItemStack item) {
        this.item = item.clone();
    }

    public ItemStack getItem() { return item.clone(); }
    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }
    public long getStock() { return stock; }
    public void setStock(long stock) { this.stock = stock; }
    public int getMaxPurchase() { return maxPurchase; }
    public void setMaxPurchase(int maxPurchase) { this.maxPurchase = maxPurchase; }
    public boolean isBuyEnabled() { return buyEnabled; }
    public void setBuyEnabled(boolean buyEnabled) { this.buyEnabled = buyEnabled; }
    public boolean isSellEnabled() { return sellEnabled; }
    public void setSellEnabled(boolean sellEnabled) { this.sellEnabled = sellEnabled; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public Map<String, Object> serialize() {
        Map<String, Object> m = new HashMap<>();
        m.put("item", ItemUtil.serializeItem(item));
        m.put("buy-price", buyPrice);
        m.put("sell-price", sellPrice);
        m.put("stock", stock);
        m.put("max-purchase", maxPurchase);
        m.put("buy-enabled", buyEnabled);
        m.put("sell-enabled", sellEnabled);
        m.put("permission", permission);
        return m;
    }

    public static ShopItem deserialize(ConfigurationSection section) {
        ItemStack item = ItemUtil.deserializeItem(section.getString("item"));
        ShopItem si = new ShopItem(item);
        si.buyPrice = section.getDouble("buy-price", -1);
        si.sellPrice = section.getDouble("sell-price", -1);
        si.stock = section.contains("stock") ? section.getLong("stock", -1) : -1;
        si.maxPurchase = section.getInt("max-purchase", -1);
        si.buyEnabled = section.getBoolean("buy-enabled", true);
        si.sellEnabled = section.getBoolean("sell-enabled", true);
        si.permission = section.getString("permission", null);
        return si;
    }
}