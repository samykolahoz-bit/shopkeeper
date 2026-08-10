package me.samy.shopkeeper.economy;

import me.samy.shopkeeper.ShopKeeperPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final ShopKeeperPlugin plugin;
    private Economy economy;
    private boolean enabled = false;

    public EconomyManager(ShopKeeperPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found - economy features disabled.");
            enabled = false;
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No economy provider found through Vault - economy features disabled.");
            enabled = false;
            return;
        }
        this.economy = rsp.getProvider();
        enabled = true;
        plugin.getLogger().info("Vault economy hooked.");
    }

    public boolean isEnabled() {
        return enabled && economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }
}