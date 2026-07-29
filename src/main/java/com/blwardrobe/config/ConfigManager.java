package com.blwardrobe.config;

import com.blwardrobe.BLWardrobePlugin;
import com.blwardrobe.util.JarResourceExtractor;
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
        // Categorias activas para pruebas.
        // Para reactivar mas adelante: {"skin", "face", "shirt", "hair", "pants", "shoes", "accessories"}
        String[] folders = {"skin", "face", "shirt", "hair", "pants"};

        for (String folder : folders) {
            File dir = new File(baseDir, folder);
            dir.mkdirs();

            // Copia (sin pisar ediciones locales) los .yml que vengan bundleados
            // en src/main/resources/categories/<folder>/ dentro del jar.
            JarResourceExtractor.extract(plugin, "categories/" + folder + "/", dir);

            File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
            if (files == null || files.length == 0) {
                // Red de seguridad: si no se bundleo nada para esta categoria,
                // se genera un archivo minimo con un item default.
                createDefaultFile(dir, folder);
                files = dir.listFiles((d, name) -> name.endsWith(".yml"));
            }
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
            String itemKey = folder + "_default";
            yaml.set("name", "&e" + capitalize(folder));
            yaml.set("slot", getDefaultSlot(folder));
            yaml.set("icon", getDefaultIcon(folder));
            yaml.set("items." + itemKey + ".name", "&fDefault " + capitalize(folder));
            yaml.set("items." + itemKey + ".permission", "blwardrobe.skin." + folder + ".default");
            yaml.set("items." + itemKey + ".default", true);
            yaml.set("items." + itemKey + ".model", folder + "_default");
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
            case "shirt" -> 2;
            case "hair" -> 3;
            case "pants" -> 4;
            case "shoes" -> 5;
            case "accessories" -> 6;
            default -> 0;
        };
    }

    private String getDefaultIcon(String folder) {
        return switch (folder) {
            case "skin", "face" -> "PLAYER_HEAD";
            case "shirt" -> "LEATHER_CHESTPLATE";
            case "hair" -> "SHEARS";
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