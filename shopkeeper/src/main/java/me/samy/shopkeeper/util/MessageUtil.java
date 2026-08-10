package me.samy.shopkeeper.util;

import me.samy.shopkeeper.ShopKeeperPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtil {
    public static void send(Player p, String key) {
        String msg = ShopKeeperPlugin.getInstance().getConfigManager().getMessage(key);
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public static String color(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }
}