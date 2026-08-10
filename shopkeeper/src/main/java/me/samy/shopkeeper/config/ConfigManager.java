package me.samy.shopkeeper.config;

import me.samy.shopkeeper.ShopKeeperPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {
    private final ShopKeeperPlugin plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public ConfigManager(ShopKeeperPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveResource("messages.yml", false);
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        String s = messages.getString(path);
        return s == null ? path : s;
    }

    public FileConfiguration getMessages() {
        return messages;
    }
}