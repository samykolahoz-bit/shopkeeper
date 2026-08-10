package me.samy.shopkeeper.shop;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.*;

public class Shop {
    private final String id;
    private String name;
    private EntityType mobType;
    private Location location;
    private String displayName;
    private ShopSettings settings = new ShopSettings();
    private GuiConfig gui = new GuiConfig();
    private final Map<String, ShopItem> items = new LinkedHashMap<>();

    public Shop(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public EntityType getMobType() { return mobType; }
    public void setMobType(EntityType mobType) { this.mobType = mobType; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public ShopSettings getSettings() { return settings; }
    public GuiConfig getGui() { return gui; }

    public Collection<ShopItem> getItems() { return items.values(); }
    public Optional<ShopItem> getItem(String key) { return Optional.ofNullable(items.get(key)); }

    public void addItem(String key, ShopItem item) { items.put(key, item); }
    public void removeItem(String key) { items.remove(key); }

    // Serialization helpers
    public void saveToFile(File file) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("shop.id", id);
        cfg.set("shop.name", name);
        cfg.set("shop.mob", mobType == null ? null : mobType.name());
        if (location != null) {
            cfg.set("shop.location.world", location.getWorld().getName());
            cfg.set("shop.location.x", location.getX());
            cfg.set("shop.location.y", location.getY());
            cfg.set("shop.location.z", location.getZ());
            cfg.set("shop.location.yaw", location.getYaw());
            cfg.set("shop.location.pitch", location.getPitch());
        }
        cfg.set("shop.display-name", displayName);
        cfg.set("settings", settings.toMap());
        cfg.set("gui", gui.toMap());
        // items
        for (Map.Entry<String, ShopItem> e : items.entrySet()) {
            cfg.set("items." + e.getKey(), e.getValue().serialize());
        }
        try {
            cfg.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Shop loadFromFile(File file) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String id = cfg.getString("shop.id");
        if (id == null) return null;
        Shop shop = new Shop(id);
        shop.name = cfg.getString("shop.name", id);
        String mobStr = cfg.getString("shop.mob", "VILLAGER");
        try { shop.mobType = EntityType.valueOf(mobStr); } catch (Exception x) { shop.mobType = EntityType.VILLAGER; }
        if (cfg.contains("shop.location.world")) {
            try {
                String world = cfg.getString("shop.location.world");
                double x = cfg.getDouble("shop.location.x");
                double y = cfg.getDouble("shop.location.y");
                double z = cfg.getDouble("shop.location.z");
                float yaw = (float) cfg.getDouble("shop.location.yaw", 0);
                float pitch = (float) cfg.getDouble("shop.location.pitch", 0);
                shop.location = new org.bukkit.Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
            } catch (Exception ignored) {}
        }
        shop.displayName = cfg.getString("shop.display-name", shop.name);
        shop.settings = ShopSettings.fromMap(cfg.getConfigurationSection("settings") == null ? new HashMap<>() : cfg.getConfigurationSection("settings").getValues(true));
        shop.gui = GuiConfig.fromMap(cfg.getConfigurationSection("gui") == null ? new HashMap<>() : cfg.getConfigurationSection("gui").getValues(true));
        if (cfg.contains("items")) {
            for (String k : cfg.getConfigurationSection("items").getKeys(false)) {
                ShopItem item = ShopItem.deserialize(cfg.getConfigurationSection("items." + k));
                shop.items.put(k, item);
            }
        }
        return shop;
    }
}