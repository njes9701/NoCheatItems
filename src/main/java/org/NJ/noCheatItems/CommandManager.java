package org.NJ.noCheatItems;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final NoCheatItems plugin;

    public CommandManager(NoCheatItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) return false;

        switch (args[0].toLowerCase()) {
            case "blacklist" -> {
                org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, NoCheatItems.GUI_TITLE);
                List<ItemStack> list = plugin.getBlacklistManager().getBlacklist();
                for (int i = 0; i < list.size() && i < 54; i++) inv.setItem(i, list.get(i).clone());
                player.openInventory(inv);
            }
            case "box" -> plugin.getQuarantineManager().openBox(player);
            case "run", "scan" -> {
                boolean delete = args[0].equalsIgnoreCase("run");
                player.sendMessage("§e開始全服掃描 (" + (delete ? "沒收" : "查看") + "模式)...");
                Scanner.runGlobalScan(plugin, player, delete);
            }
            case "autorun" -> {
                plugin.getBlacklistManager().toggleAutoScan();
                player.sendMessage("§e自動偵測已 " + (plugin.getBlacklistManager().isAutoScanEnabled() ? "§a開啟" : "§c關閉"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("blacklist", "run", "scan", "autorun", "box"), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}