package org.NJ.noCheatItems;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public final class NoCheatItems extends JavaPlugin {
    public static final String GUI_TITLE = "§0作弊物品管理庫";
    private BlacklistManager blacklistManager;
    private QuarantineManager quarantineManager;

    @Override
    public void onEnable() {
        this.blacklistManager = new BlacklistManager(this);
        this.quarantineManager = new QuarantineManager(this);
        
        CommandManager cmdManager = new CommandManager(this);
        Objects.requireNonNull(getCommand("nocheatitem")).setExecutor(cmdManager);
        Objects.requireNonNull(getCommand("nocheatitem")).setTabCompleter(cmdManager);
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("NoCheatItems (含隔離區功能) 已啟動");
    }

    public BlacklistManager getBlacklistManager() { return blacklistManager; }
    public QuarantineManager getQuarantineManager() { return quarantineManager; }
}
