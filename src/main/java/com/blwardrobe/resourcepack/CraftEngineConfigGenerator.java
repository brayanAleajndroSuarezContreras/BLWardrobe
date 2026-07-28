package com.blwardrobe.resourcepack;

import com.blwardrobe.BLWardrobePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Genera, por cada parte definida en config.yml -> part-models, un archivo
 * plugins/CraftEngine/resources/BLWardrobe/configuration/<parte>.yml con un
 * item por CADA combinacion posible de las categorias involucradas en esa
 * parte (producto cartesiano). Ej: si skin tiene 2 items y shirts tiene 3,
 * "body" genera 6 items (uno por combinacion skin x shirt).
 */
public class CraftEngineConfigGenerator {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");

    private final BLWardrobePlugin plugin;
    private final File outputDir; // plugins/CraftEngine/resources/BLWardrobe/configuration

    public CraftEngineConfigGenerator(BLWardrobePlugin plugin, File craftEngineDir) {
        this.plugin = plugin;
        this.outputDir = new File(craftEngineDir, "resources/BLWardrobe/configuration");
    }

    public boolean generate() {
        ConfigurationSection partModels = plugin.getConfig().getConfigurationSection("part-models");
        ConfigurationSection genConfig = plugin.getConfig().getConfigurationSection("craftengine-generation");
        if (partModels == null || genConfig == null) {
            plugin.getLogger().warning("Falta 'part-models' o 'craftengine-generation' en config.yml, no se genero nada.");
            return false;
        }

        String namespace = genConfig.getString("namespace", "blwardrobe");
        String material = genConfig.getString("material", "paper");
        ConfigurationSection partsSection = genConfig.getConfigurationSection("parts");
        if (partsSection == null) {
            plugin.getLogger().warning("Falta 'craftengine-generation.parts' en config.yml, no se genero nada.");
            return false;
        }

        Map<String, YamlConfiguration> categoryConfigs = plugin.getConfigManager().getCategoryConfigs();
        outputDir.mkdirs();

        int totalItems = 0;
        int totalFiles = 0;

        for (String part : partModels.getKeys(false)) {
            String template = partModels.getString(part);
            ConfigurationSection partGen = partsSection.getConfigurationSection(part);
            if (template == null || partGen == null) {
                plugin.getLogger().warning("No hay 'craftengine-generation.parts." + part + "' configurado, se omite esa parte.");
                continue;
            }

            List<String> categoryIds = extractPlaceholders(template);
            List<List<String>> itemIdLists = new ArrayList<>();
            boolean missing = false;
            for (String catId : categoryIds) {
                YamlConfiguration catConfig = categoryConfigs.get(catId);
                ConfigurationSection items = catConfig != null ? catConfig.getConfigurationSection("items") : null;
                if (items == null) {
                    plugin.getLogger().warning("La categoria '" + catId + "' (usada en part-models." + part + ") no tiene items cargados.");
                    missing = true;
                    break;
                }
                itemIdLists.add(new ArrayList<>(items.getKeys(false)));
            }
            if (missing) continue;

            String parent = partGen.getString("parent");
            ConfigurationSection texturesSection = partGen.getConfigurationSection("textures");

            YamlConfiguration yaml = new YamlConfiguration();

            for (Map<String, String> combo : cartesianProduct(categoryIds, itemIdLists)) {
                String suffix = template;
                for (String catId : categoryIds) {
                    suffix = suffix.replace("{" + catId + "}", combo.get(catId));
                }
                String itemId = namespace + ":" + suffix;
                String base = "items." + itemId;

                yaml.set(base + ".material", material);
                yaml.set(base + ".model.type", "minecraft:model");
                // ${__ID__} es una variable propia de CraftEngine (se resuelve solo,
                // no la tocamos nosotros), NO se interpola aca.
                yaml.set(base + ".model.path", namespace + ":item/mannequin/${__ID__}");
                if (parent != null) {
                    yaml.set(base + ".model.generation.parent", namespace + ":" + parent);
                }

                if (texturesSection != null) {
                    for (String texVar : texturesSection.getKeys(false)) {
                        String pathTemplate = texturesSection.getString(texVar);
                        if (pathTemplate == null) continue;
                        String resolved = pathTemplate;
                        for (String catId : categoryIds) {
                            resolved = resolved.replace("${" + catId + "}", combo.get(catId));
                        }
                        yaml.set(base + ".model.generation.textures." + texVar, namespace + ":" + resolved);
                    }
                }

                totalItems++;
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

    private List<String> extractPlaceholders(String template) {
        List<String> result = new ArrayList<>();
        Matcher m = PLACEHOLDER.matcher(template);
        while (m.find()) result.add(m.group(1));
        return result;
    }

    private List<Map<String, String>> cartesianProduct(List<String> keys, List<List<String>> lists) {
        List<Map<String, String>> result = new ArrayList<>();
        result.add(new LinkedHashMap<>());
        for (int i = 0; i < keys.size(); i++) {
            List<Map<String, String>> next = new ArrayList<>();
            for (Map<String, String> partial : result) {
                for (String itemId : lists.get(i)) {
                    Map<String, String> copy = new LinkedHashMap<>(partial);
                    copy.put(keys.get(i), itemId);
                    next.add(copy);
                }
            }
            result = next;
        }
        return result;
    }
}