package me.samy.shopkeeper;

import me.samy.shopkeeper.command.ShopKeeperCommand;
import me.samy.shopkeeper.config.ConfigManager;
import me.samy.shopkeeper.economy.EconomyManager;
import me.samy.shopkeeper.entity.ShopEntityManager;
import me.samy.shopkeeper.gui.ShopGUI;
import me.samy.shopkeeper.listener.ShopListener;
import me.samy.shopkeeper.persistence.DatabaseManager;
import me.samy.shopkeeper.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

public final class ShopKeeperPlugin extends JavaPlugin {

    private static ShopKeeperPlugin instance;
    private ConfigManager configManager;
    private ShopManager shopManager;
    private ShopEntityManager entityManager;
    private EconomyManager economyManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.configManager.load();
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.init();

        this.economyManager = new EconomyManager(this);
        this.economyManager.setupEconomy();

        this.shopManager = new ShopManager(this);
        this.shopManager.loadAllShops();

        this.entityManager = new ShopEntityManager(this, shopManager);
        this.entityManager.ensureAllEntities();

        // Commands
        ShopKeeperCommand command = new ShopKeeperCommand(this, shopManager, entityManager);
        getCommand("shopkeeper").setExecutor(command);
        getCommand("shopkeeper").setTabCompleter(command);

        // Listeners
        getServer().getPluginManager().registerEvents(new ShopListener(this, shopManager, entityManager), this);

        // GUI helper (just to ensure class load)
        ShopGUI.register(this);

        getLogger().info("ShopKeeper enabled.");
    }

    @Override
    public void onDisable() {
        shopManager.saveAllShopsSync();
        if (databaseManager != null) databaseManager.shutdown();
        getLogger().info("ShopKeeper disabled.");
    }

    public static ShopKeeperPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ShopEntityManager getEntityManager() {
        return entityManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}