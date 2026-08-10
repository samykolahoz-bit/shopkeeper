package me.samy.shopkeeper.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.io.*;

public class ItemUtil {
    static {
        // nothing required
    }

    public static String getDisplayName(ItemStack is) {
        if (is == null) return "";
        ItemMeta m = is.getItemMeta();
        if (m != null && m.hasDisplayName()) return ChatColor.stripColor(m.getDisplayName());
        return is.getType().name();
    }

    public static boolean isSimilar(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        if (a.getType() != b.getType()) return false;
        ItemMeta ma = a.getItemMeta();
        ItemMeta mb = b.getItemMeta();
        if (ma == null && mb == null) return a.getAmount() == b.getAmount();
        // compare display name
        if (ma.hasDisplayName() || mb.hasDisplayName()) {
            String na = ma.hasDisplayName() ? ma.getDisplayName() : "";
            String nb = mb.hasDisplayName() ? mb.getDisplayName() : "";
            if (!na.equals(nb)) return false;
        }
        // compare lore
        if (ma.hasLore() || mb.hasLore()) {
            List<String> la = ma.hasLore() ? ma.getLore() : null;
            List<String> lb = mb.hasLore() ? mb.getLore() : null;
            if (la == null) la = List.of();
            if (lb == null) lb = List.of();
            if (!la.equals(lb)) return false;
        }
        // For production, you may want stricter checks (enchantments, custom model)
        return true;
    }

    public static ItemStack makeBorderPane() {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(" ");
        it.setItemMeta(meta);
        return it;
    }

    public static ItemStack makeControlItem(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        it.setItemMeta(meta);
        return it;
    }

    // Simplified item serialization to string using base64; for production consider more compact forms.
    public static String serializeItem(ItemStack item) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(baos);
            out.writeObject(item);
            out.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static ItemStack deserializeItem(String base64) {
        if (base64 == null || base64.isEmpty()) return new ItemStack(Material.STONE);
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            BukkitObjectInputStream in = new BukkitObjectInputStream(bais);
            Object obj = in.readObject();
            in.close();
            return (ItemStack) obj;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ItemStack(Material.STONE);
        }
    }

    // Inner helper classes for Bukkit object streams
    private static class BukkitObjectOutputStream extends ObjectOutputStream {
        public BukkitObjectOutputStream(OutputStream out) throws IOException { super(out); enableReplaceObject(true); }
    }
    private static class BukkitObjectInputStream extends ObjectInputStream {
        public BukkitObjectInputStream(InputStream in) throws IOException { super(in); }
    }

    public static void addShopLore(ItemStack is, me.samy.shopkeeper.shop.ShopItem si) {
        ItemMeta m = is.getItemMeta();
        List<String> lore = m.hasLore() ? m.getLore() : List.of();
        List<String> newLore = lore.stream().collect(Collectors.toList());
        if (si.getBuyPrice() >= 0) newLore.add(ChatColor.GOLD + "Buy: $" + si.getBuyPrice());
        if (si.getSellPrice() >= 0) newLore.add(ChatColor.YELLOW + "Sell: $" + si.getSellPrice());
        if (si.getStock() >= 0) newLore.add(ChatColor.GRAY + "Stock: " + si.getStock());
        m.setLore(newLore);
        is.setItemMeta(m);
    }
}