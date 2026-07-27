package com.blwardrobe.config;

import com.blwardrobe.BLWardrobePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigManager {
    private final BLWardrobePlugin plugin;
    private final Map<String, YamlConfiguration> categoryConfigs = new LinkedHashMap<>();

    public ConfigManager(BLWardrobePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        categoryConfigs.clear();
        File baseDir = plugin.getDataFolder();
        String[] folders = {"skin", "face", "shirts", "pants", "shoes", "accessories"};

        for (String folder : folders) {
            File dir = new File(baseDir, folder);
            if (!dir.exists()) {
                dir.mkdirs();
                createDefaultFile(dir, folder);
            }

            File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
            if (files == null) continue;

            for (File file : files) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                categoryConfigs.merge(folder, yaml, (existing, added) -> {
                    var items = added.getConfigurationSection("items");
                    if (items != null) {
                        for (String key : items.getKeys(false)) {
                            existing.set("items." + key, items.get(key));
                        }
                    }
                    return existing;
                });
            }
        }
    }

    private void createDefaultFile(File dir, String folder) {
        File defaultFile = new File(dir, folder + ".yml");
        try {
            defaultFile.createNewFile();
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("name", "&e" + capitalize(folder));
            yaml.set("slot", getDefaultSlot(folder));
            yaml.set("icon", getDefaultIcon(folder));
            yaml.set("items.default.name", "&fDefault " + capitalize(folder));
            yaml.set("items.default.permission", "blwardrobe.skin." + folder + ".default");
            yaml.set("items.default.default", true);
            yaml.set("items.default.model", folder + "_default");
            yaml.save(defaultFile);
        } catch (IOException e) {
            plugin.getLogger().warning("No se pudo crear " + defaultFile.getName());
        }
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int getDefaultSlot(String folder) {
        return switch (folder) {
            case "skin" -> 0;
            case "face" -> 1;
            case "shirts" -> 2;
            case "pants" -> 3;
            case "shoes" -> 4;
            case "accessories" -> 5;
            default -> 0;
        };
    }

    private String getDefaultIcon(String folder) {
        return switch (folder) {
            case "skin", "face" -> "PLAYER_HEAD";
            case "shirts" -> "LEATHER_CHESTPLATE";
            case "pants" -> "LEATHER_LEGGINGS";
            case "shoes" -> "LEATHER_BOOTS";
            case "accessories" -> "GOLDEN_HELMET";
            default -> "PAPER";
        };
    }

    public Map<String, YamlConfiguration> getCategoryConfigs() {
        return categoryConfigs;
    }

    public YamlConfiguration getCategory(String id) {
        return categoryConfigs.get(id);
    }
}