package me.samy.shopkeeper.shop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Shop {

    private final String id;
    private String name;
    private EntityType mobType;
    private Location location;
    private String displayName;
    private ShopSettings settings = new ShopSettings();
    private GuiConfig gui = new GuiConfig();

    private final Map<String, ShopItem> items =
            new LinkedHashMap<>();

    public Shop(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityType getMobType() {
        return mobType;
    }

    public void setMobType(EntityType mobType) {
        this.mobType = mobType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ShopSettings getSettings() {
        return settings;
    }

    public GuiConfig getGui() {
        return gui;
    }

    public Collection<ShopItem> getItems() {
        return items.values();
    }

    public Optional<ShopItem> getItem(String key) {
        return Optional.ofNullable(items.get(key));
    }

    public void addItem(
            String key,
            ShopItem item
    ) {
        items.put(key, item);
    }

    public void removeItem(String key) {
        items.remove(key);
    }

    // Serialization helpers

    public void saveToFile(File file) {

        FileConfiguration cfg =
                YamlConfiguration.loadConfiguration(file);

        cfg.set(
                "shop.id",
                id
        );

        cfg.set(
                "shop.name",
                name
        );

        cfg.set(
                "shop.mob",
                mobType == null
                        ? null
                        : mobType.name()
        );

        if (location != null) {

            if (location.getWorld() != null) {

                cfg.set(
                        "shop.location.world",
                        location.getWorld().getName()
                );
            }

            cfg.set(
                    "shop.location.x",
                    location.getX()
            );

            cfg.set(
                    "shop.location.y",
                    location.getY()
            );

            cfg.set(
                    "shop.location.z",
                    location.getZ()
            );

            cfg.set(
                    "shop.location.yaw",
                    location.getYaw()
            );

            cfg.set(
                    "shop.location.pitch",
                    location.getPitch()
            );
        }

        cfg.set(
                "shop.display-name",
                displayName
        );

        cfg.set(
                "settings",
                settings.toMap()
        );

        cfg.set(
                "gui",
                gui.toMap()
        );

        // Items

        for (Map.Entry<String, ShopItem> entry :
                items.entrySet()) {

            cfg.set(
                    "items." + entry.getKey(),
                    entry.getValue().serialize()
            );
        }

        try {

            cfg.save(file);

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    public static Shop loadFromFile(File file) {

        FileConfiguration cfg =
                YamlConfiguration.loadConfiguration(file);

        String id =
                cfg.getString("shop.id");

        if (id == null) {
            return null;
        }

        Shop shop =
                new Shop(id);

        shop.name =
                cfg.getString(
                        "shop.name",
                        id
                );

        String mobStr =
                cfg.getString(
                        "shop.mob",
                        "VILLAGER"
                );

        try {

            shop.mobType =
                    EntityType.valueOf(
                            mobStr.toUpperCase()
                    );

        } catch (Exception ignored) {

            shop.mobType =
                    EntityType.VILLAGER;
        }

        if (cfg.contains(
                "shop.location.world"
        )) {

            try {

                String worldName =
                        cfg.getString(
                                "shop.location.world"
                        );

                double x =
                        cfg.getDouble(
                                "shop.location.x"
                        );

                double y =
                        cfg.getDouble(
                                "shop.location.y"
                        );

                double z =
                        cfg.getDouble(
                                "shop.location.z"
                        );

                float yaw =
                        (float) cfg.getDouble(
                                "shop.location.yaw",
                                0
                        );

                float pitch =
                        (float) cfg.getDouble(
                                "shop.location.pitch",
                                0
                        );

                /*
                 * Bukkit.getWorld(worldName)
                 * requires the Bukkit import above.
                 */
                org.bukkit.World world =
                        Bukkit.getWorld(worldName);

                if (world != null) {

                    shop.location =
                            new Location(
                                    world,
                                    x,
                                    y,
                                    z,
                                    yaw,
                                    pitch
                            );
                }

            } catch (Exception ignored) {
                // Invalid location data is ignored.
            }
        }

        shop.displayName =
                cfg.getString(
                        "shop.display-name",
                        shop.name
                );

        if (cfg.getConfigurationSection(
                "settings"
        ) != null) {

            shop.settings =
                    ShopSettings.fromMap(
                            cfg.getConfigurationSection(
                                    "settings"
                            ).getValues(true)
                    );

        } else {

            shop.settings =
                    ShopSettings.fromMap(
                            new HashMap<>()
                    );
        }

        if (cfg.getConfigurationSection(
                "gui"
        ) != null) {

            shop.gui =
                    GuiConfig.fromMap(
                            cfg.getConfigurationSection(
                                    "gui"
                            ).getValues(true)
                    );

        } else {

            shop.gui =
                    GuiConfig.fromMap(
                            new HashMap<>()
                    );
        }

        if (cfg.getConfigurationSection(
                "items"
        ) != null) {

            for (String key :
                    cfg.getConfigurationSection(
                            "items"
                    ).getKeys(false)) {

                ShopItem item =
                        ShopItem.deserialize(
                                cfg.getConfigurationSection(
                                        "items." + key
                                )
                        );

                if (item != null) {

                    shop.items.put(
                            key,
                            item
                    );
                }
            }
        }

        return shop;
    }
}
