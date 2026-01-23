package org.NJ.noCheatItems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Scanner {

    public static void runGlobalScan(NoCheatItems plugin, Player admin, boolean delete) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.hasPermission("nocheatitems.admin")) continue;
            target.getScheduler().run(plugin, t -> scanPlayer(plugin, target, admin, delete), null);
        }
    }

    public static void scanPlayer(NoCheatItems plugin, Player target, Player admin, boolean delete) {
        if (target.hasPermission("nocheatitems.admin")) return;

        // 直接從 Manager 取得預先快取好的特徵
        List<ItemFeature> targets = plugin.getBlacklistManager().getTargetFeatures();
        if (targets.isEmpty()) return;

        scanInventory(plugin, target, target.getInventory(), "背包", targets, admin, delete);
        scanInventory(plugin, target, target.getEnderChest(), "終界箱", targets, admin, false);
    }

    private static void scanInventory(NoCheatItems plugin, Player p, Inventory inv, String containerName, List<ItemFeature> targets, Player admin, boolean delete) {
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;

            ItemFeature current = ItemFeature.from(item);
            if (current == null) continue;

            for (ItemFeature target : targets) {
                if (target.matches(current)) {
                    report(plugin, p, item, containerName, delete, admin);
                    if (delete) {
                        plugin.getQuarantineManager().addItem(item);
                        inv.setItem(i, null);
                    }
                    break; // 匹配到一個就處理下一個物品
                }
            }
        }
    }

    private static void report(NoCheatItems plugin, Player p, ItemStack item, String container, boolean del, Player admin) {
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
        String loc = String.format("%s (%d,%d,%d)", p.getWorld().getName(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
        
        String prefix = del ? "§4[沒收]" : "§e[偵測]";
        String msg = String.format("%s §f玩家: §b%s §f| 位置: §7%s §f| 容器: §a%s §f| 物品: §r%s", 
            prefix, p.getName(), loc, container, itemName);

        if (admin != null) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> admin.sendMessage(msg));
        } else {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.hasPermission("nocheatitems.admin")) {
                        online.sendMessage("§6[自動偵測報告] " + msg);
                    }
                }
            });
        }

        // 非同步寫入日誌，避免阻塞 Region Thread
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            String logType = del ? "CONFISCATED" : "SCANNED";
            String logEntry = String.format("[%s] [%s] 玩家: %s | 位置: %s | 容器: %s | 物品: %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 
                logType, p.getName(), loc, container, itemName);
                
            try (PrintWriter out = new PrintWriter(new FileWriter(new File(plugin.getDataFolder(), "logs.txt"), true))) {
                out.println(logEntry);
            } catch (IOException ignored) {}
        });
    }
}
