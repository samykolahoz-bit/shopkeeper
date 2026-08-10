package me.samy.shopkeeper.command;

import me.samy.shopkeeper.ShopKeeperPlugin;
import me.samy.shopkeeper.entity.ShopEntityManager;
import me.samy.shopkeeper.shop.Shop;
import me.samy.shopkeeper.shop.ShopManager;
import me.samy.shopkeeper.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopKeeperCommand implements CommandExecutor, TabCompleter {
    private final ShopKeeperPlugin plugin;
    private final ShopManager shopManager;
    private final ShopEntityManager entityManager;

    public ShopKeeperCommand(ShopKeeperPlugin plugin, ShopManager shopManager, ShopEntityManager entityManager) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.entityManager = entityManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender);
            case "reload":
                return handleReload(sender);
            case "additem":
                return handleAddItem(sender, args);
            case "teleport":
                return handleTeleport(sender, args);
            default:
                sendHelp(sender);
        }
        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shopkeeper.create")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may create shops.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /shopkeeper create <id> <mob>");
            return true;
        }
        String id = args[1].toLowerCase();
        String mob = args[2].toUpperCase();
        Shop s = shopManager.createShop(id, id);
        try {
            s.setMobType(org.bukkit.entity.EntityType.valueOf(mob));
        } catch (Exception ex) {
            s.setMobType(org.bukkit.entity.EntityType.VILLAGER);
        }
        Player p = (Player) sender;
        s.setLocation(p.getLocation());
        s.setDisplayName("&a" + id);
        shopManager.saveShopAsync(s);
        entityManager.spawnShopkeeper(s);
        sender.sendMessage(ChatColor.GREEN + "Shop " + id + " created.");
        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shopkeeper.delete")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /shopkeeper delete <id>");
            return true;
        }
        String id = args[1];
        shopManager.deleteShop(id);
        sender.sendMessage(ChatColor.GREEN + "Shop deleted: " + id);
        return true;
    }

    private boolean handleList(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Shops:");
        for (Shop s : shopManager.getShops()) {
            sender.sendMessage(ChatColor.YELLOW + "- " + s.getId() + " (" + s.getName() + ")");
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("shopkeeper.reload")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        plugin.getConfig().options().copyDefaults(true);
        plugin.reloadConfig();
        plugin.getConfigManager().load();
        shopManager.loadAllShops();
        entityManager.ensureAllEntities();
        sender.sendMessage(ChatColor.GREEN + "ShopKeeper reloaded.");
        return true;
    }

    private boolean handleAddItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shopkeeper.edit")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use additem.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /shopkeeper additem <shopId>");
            return true;
        }
        String id = args[1];
        Shop s = shopManager.getShop(id).orElse(null);
        if (s == null) {
            sender.sendMessage(ChatColor.RED + "No such shop: " + id);
            return true;
        }
        Player p = (Player) sender;
        org.bukkit.inventory.ItemStack inhand = p.getInventory().getItemInMainHand();
        if (inhand == null || inhand.getType().isAir()) {
            p.sendMessage(ChatColor.RED + "Hold an item in your hand to add.");
            return true;
        }
        // create key using material + timestamp to avoid collisions
        String key = inhand.getType().name().toLowerCase() + "_" + System.currentTimeMillis();
        me.samy.shopkeeper.shop.ShopItem si = new me.samy.shopkeeper.shop.ShopItem(inhand);
        si.setBuyPrice(0);
        si.setSellPrice(0);
        s.addItem(key, si);
        shopManager.saveShopAsync(s);
        p.sendMessage(ChatColor.GREEN + "Item added to shop " + id + ". Use /shopkeeper price and /shopkeeper stock to configure.");
        return true;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shopkeeper.teleport")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may teleport to shops.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /shopkeeper teleport <id>");
            return true;
        }
        String id = args[1];
        Shop s = shopManager.getShop(id).orElse(null);
        if (s == null || s.getLocation() == null) {
            sender.sendMessage(ChatColor.RED + "No such shop or location unknown.");
            return true;
        }
        ((Player) sender).teleport(s.getLocation());
        sender.sendMessage(ChatColor.GREEN + "Teleported to shop " + id);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== ShopKeeper Help ===");
        if (sender.hasPermission("shopkeeper.create")) sender.sendMessage(ChatColor.YELLOW + "/shopkeeper create <id> <mob> - Create shop");
        if (sender.hasPermission("shopkeeper.delete")) sender.sendMessage(ChatColor.YELLOW + "/shopkeeper delete <id> - Delete shop");
        if (sender.hasPermission("shopkeeper.edit")) sender.sendMessage(ChatColor.YELLOW + "/shopkeeper additem <id> - Add item (hold item)");
        sender.sendMessage(ChatColor.YELLOW + "/shopkeeper list - List shops");
        if (sender.hasPermission("shopkeeper.reload")) sender.sendMessage(ChatColor.YELLOW + "/shopkeeper reload - Reload plugin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> res = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = List.of("create","delete","edit","list","info","additem","removeitem","setprice","price","stock","setstock","reload","teleport","clone","enable","disable","help","setup");
            for (String s : subs) if (s.startsWith(args[0].toLowerCase())) res.add(s);
            return res;
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("create")) {
                // suggest some mobs
                return List.of("VILLAGER","WANDERING_TRADER","ZOMBIE","IRON_GOLEM").stream()
                        .filter(x -> x.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
            // suggest shop ids
            return shopManager.getShops().stream().map(Shop::getId).filter(x -> x.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return res;
    }
}