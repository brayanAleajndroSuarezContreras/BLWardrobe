package com.blwardrobe.resourcepack;

import com.blwardrobe.BLWardrobePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Genera, por cada capa definida en config.yml -> mannequin-layers.parts,
 * UN item de CraftEngine por CADA item de la categoria que maneja esa capa.
 *
 * A diferencia del esquema anterior (producto cartesiano entre categorias),
 * cada capa es independiente: si "skin" tiene 5 items y "shirts" tiene 8,
 * "body" genera 5 + 8 = 13 items (no 40). La cantidad crece lineal, no
 * combinatoriamente, sin importar cuantas categorias tenga una parte.
 */
public class CraftEngineConfigGenerator {

    private final BLWardrobePlugin plugin;
    private final File outputDir; // plugins/CraftEngine/resources/BLWardrobe/configuration

    public CraftEngineConfigGenerator(BLWardrobePlugin plugin, File craftEngineDir) {
        this.plugin = plugin;
        this.outputDir = new File(craftEngineDir, "resources/BLWardrobe/configuration");
    }

    public boolean generate() {
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("mannequin-layers");
        if (root == null) {
            plugin.getLogger().warning("Falta 'mannequin-layers' en config.yml, no se genero nada.");
            return false;
        }

        String namespace = root.getString("namespace", "blwardrobe");
        String material = root.getString("material", "paper");
        ConfigurationSection partsSection = root.getConfigurationSection("parts");
        if (partsSection == null) {
            plugin.getLogger().warning("Falta 'mannequin-layers.parts' en config.yml, no se genero nada.");
            return false;
        }

        Map<String, YamlConfiguration> categoryConfigs = plugin.getConfigManager().getCategoryConfigs();
        outputDir.mkdirs();

        int totalItems = 0;
        int totalFiles = 0;

        for (String part : partsSection.getKeys(false)) {
            List<Map<?, ?>> layerConfigs = partsSection.getMapList(part);
            if (layerConfigs.isEmpty()) continue;

            YamlConfiguration yaml = new YamlConfiguration();

            for (Map<?, ?> layer : layerConfigs) {
                String category = str(layer.get("category"));
                String idTemplate = str(layer.get("id"));
                String parent = str(layer.get("parent"));
                String textureVar = layer.containsKey("texture-var") ? str(layer.get("texture-var")) : category;
                String textureTemplate = str(layer.get("texture"));

                if (category == null || idTemplate == null) {
                    plugin.getLogger().warning("Capa invalida en mannequin-layers.parts." + part + " (falta category o id), se omite.");
                    continue;
                }

                YamlConfiguration catConfig = categoryConfigs.get(category);
                ConfigurationSection items = catConfig != null ? catConfig.getConfigurationSection("items") : null;
                if (items == null) {
                    plugin.getLogger().warning("La categoria '" + category + "' (usada en mannequin-layers.parts." + part + ") no tiene items cargados.");
                    continue;
                }

                for (String itemId : items.getKeys(false)) {
                    String suffix = idTemplate.replace("{" + category + "}", itemId);
                    String fullId = namespace + ":" + suffix;
                    String base = "items." + fullId;

                    yaml.set(base + ".material", material);
                    yaml.set(base + ".model.type", "minecraft:model");
                    // ${__ID__} es una variable propia de CraftEngine (se resuelve sola),
                    // no la interpolamos aca.
                    yaml.set(base + ".model.path", namespace + ":item/mannequin/${__ID__}");
                    if (parent != null) {
                        yaml.set(base + ".model.generation.parent", namespace + ":" + parent);
                    }
                    if (textureTemplate != null) {
                        String resolvedTexture = textureTemplate.replace("${" + category + "}", itemId);
                        yaml.set(base + ".model.generation.textures." + textureVar, namespace + ":" + resolvedTexture);
                    }

                    totalItems++;
                }
            }

            File outFile = new File(outputDir, part + ".yml");
            try {
                yaml.save(outFile);
                totalFiles++;
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo guardar " + outFile.getName() + ": " + e.getMessage());
                return false;
            }
        }

        plugin.getLogger().info("Configuracion de CraftEngine generada: " + totalItems
                + " items en " + totalFiles + " archivos (" + outputDir.getAbsolutePath() + ")");
        return true;
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}