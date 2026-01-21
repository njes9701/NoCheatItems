package org.NJ.noCheatItems;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuarantineManager {
    private final NoCheatItems plugin;
    private final List<ItemStack> quarantinedItems = new ArrayList<>();
    private final File file;
    public static final String BOX_TITLE = "§4作弊物品隔離區";

    public QuarantineManager(NoCheatItems plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "quarantine.yml");
        load();
    }

    private void load() {
        if (!file.exists()) return;
        List<?> list = YamlConfiguration.loadConfiguration(file).getList("items");
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof ItemStack item) quarantinedItems.add(item);
            }
        }
    }

    public synchronized void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items", quarantinedItems);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addItem(ItemStack item) {
        quarantinedItems.add(item.clone());
        save();
    }

    public void openBox(org.bukkit.entity.Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, BOX_TITLE);
        // 只顯示最後 54 個被沒收的物品
        int start = Math.max(0, quarantinedItems.size() - 54);
        for (int i = 0; i < 54 && (start + i) < quarantinedItems.size(); i++) {
            inv.setItem(i, quarantinedItems.get(start + i));
        }
        admin.openInventory(inv);
    }

    public synchronized void updateFromInventory(Inventory inv) {
        // 簡單化處理：管理員從 GUI 拿走就當作處理掉了
        quarantinedItems.clear();
        for (ItemStack item : inv.getContents()) {
            if (item != null) quarantinedItems.add(item.clone());
        }
        save();
    }
}
