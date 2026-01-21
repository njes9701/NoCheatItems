package org.NJ.noCheatItems;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlacklistManager {
    private final NoCheatItems plugin;
    private final List<ItemStack> blacklist = new ArrayList<>();
    private List<ItemFeature> cachedFeatures = new ArrayList<>();
    private boolean autoScanEnabled = true;
    private final File itemsFile;
    private final File settingsFile;

    public BlacklistManager(NoCheatItems plugin) {
        this.plugin = plugin;
        this.itemsFile = new File(plugin.getDataFolder(), "items.yml");
        this.settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        load();
    }

    public void load() {
        blacklist.clear();
        if (itemsFile.exists()) {
            List<?> list = YamlConfiguration.loadConfiguration(itemsFile).getList("items");
            if (list != null) {
                for (Object obj : list) {
                    if (obj instanceof ItemStack item) blacklist.add(item);
                }
            }
        }
        rebuildCache();
        if (settingsFile.exists()) {
            autoScanEnabled = YamlConfiguration.loadConfiguration(settingsFile).getBoolean("auto", true);
        }
    }

    public void rebuildCache() {
        this.cachedFeatures = blacklist.stream().map(ItemFeature::from).toList();
    }

    public void save(List<ItemStack> newItems) {
        if (newItems != this.blacklist) {
            this.blacklist.clear();
            this.blacklist.addAll(newItems);
        }
        rebuildCache();
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.set("items", this.blacklist);
            config.save(itemsFile);
            
            YamlConfiguration setConfig = new YamlConfiguration();
            setConfig.set("auto", autoScanEnabled);
            setConfig.save(settingsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toggleAutoScan() {
        this.autoScanEnabled = !autoScanEnabled;
        save(this.blacklist);
    }

    public List<ItemStack> getBlacklist() { return blacklist; }
    public boolean isAutoScanEnabled() { return autoScanEnabled; }
    public List<ItemFeature> getTargetFeatures() {
        return cachedFeatures;
    }
}
