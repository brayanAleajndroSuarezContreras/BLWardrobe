package com.blwardrobe;

import com.blwardrobe.config.ConfigManager;
import com.blwardrobe.wardrobe.WardrobeManager;
import com.blwardrobe.skin.SkinServiceManager;
import com.blwardrobe.storage.StorageManager;
import com.blwardrobe.resourcepack.RPManager;
import com.blwardrobe.listener.PlayerListener;
import com.blwardrobe.command.BLWCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class BLWardrobePlugin extends JavaPlugin {

    private static BLWardrobePlugin instance;
    private ConfigManager configManager;
    private WardrobeManager wardrobeManager;
    private SkinServiceManager skinServiceManager;
    private StorageManager storageManager;
    private RPManager rpManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();

        this.configManager = new ConfigManager(this);
        configManager.loadAll();

        this.storageManager = new StorageManager(this);
        this.rpManager = new RPManager(this);
        this.skinServiceManager = new SkinServiceManager(this);
        this.wardrobeManager = new WardrobeManager(this);

        getCommand("blwardrobe").setExecutor(new BLWCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("BLWardrobe habilitado. " + configManager.getCategoryConfigs().size() + " categorias cargadas.");
    }

    @Override
    public void onDisable() {
        if (wardrobeManager != null) wardrobeManager.shutdown();
        if (storageManager != null) storageManager.shutdown();
    }

    public static BLWardrobePlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public WardrobeManager getWardrobeManager() { return wardrobeManager; }
    public SkinServiceManager getSkinServiceManager() { return skinServiceManager; }
    public StorageManager getStorageManager() { return storageManager; }
    public RPManager getRpManager() { return rpManager; }
}