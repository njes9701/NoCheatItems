package org.NJ.noCheatItems;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {
    private final NoCheatItems plugin;

    public PlayerListener(NoCheatItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // 檢查是否為插件開啟的虛擬 Inventory (Holder 為 null)
        if (event.getInventory().getHolder() != null) return;
        
        String title = event.getView().getTitle();
        if (title.equals(NoCheatItems.GUI_TITLE)) {
            List<ItemStack> items = new ArrayList<>();
            for (ItemStack i : event.getInventory().getContents()) {
                if (i != null && i.getType() != Material.AIR) items.add(i.clone());
            }
            // 更新黑名單
            plugin.getBlacklistManager().save(items);
            event.getPlayer().sendMessage("§a黑名單清單已更新並儲存。");
        } else if (title.equals(QuarantineManager.BOX_TITLE)) {
            plugin.getQuarantineManager().updateFromInventory(event.getInventory());
            event.getPlayer().sendMessage("§e隔離區已更新。");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getBlacklistManager().isAutoScanEnabled()) {
            org.bukkit.entity.Player player = event.getPlayer();
            // 跳過管理員
            if (player.hasPermission("nocheatitems.admin")) return;
            
            player.getScheduler().run(plugin, t -> 
                Scanner.scanPlayer(plugin, player, null, true)
            , null);
        }
    }
}