package com.blwardrobe.wardrobe.skin;

import com.blwardrobe.BLWardrobePlugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resuelve, para la seleccion actual del jugador (SkinState), el modelo que
 * le corresponde a CADA CAPA de cada parte del maniquin, segun
 * config.yml -> mannequin-layers.parts.
 *
 * A diferencia del esquema anterior (una sola plantilla combinando varias
 * categorias por parte), aca cada capa depende de UNA sola categoria, asi
 * que no hay combinacion entre ellas.
 */
public class PartModelResolver {
    private final BLWardrobePlugin plugin;

    // parte -> lista de capas [{category, idTemplate}], en orden de renderizado
    private final Map<String, List<Layer>> partLayers = new LinkedHashMap<>();

    public record Layer(String category, String idTemplate) {}

    public PartModelResolver(BLWardrobePlugin plugin) {
        this.plugin = plugin;
        loadLayers();
    }

    private void loadLayers() {
        var partsSection = plugin.getConfig().getConfigurationSection("mannequin-layers.parts");
        if (partsSection == null) return;

        for (String part : partsSection.getKeys(false)) {
            List<Map<?, ?>> layerConfigs = partsSection.getMapList(part);
            List<Layer> layers = new java.util.ArrayList<>();
            for (Map<?, ?> raw : layerConfigs) {
                Object category = raw.get("category");
                Object id = raw.get("id");
                if (category == null || id == null) continue;
                layers.add(new Layer(String.valueOf(category), String.valueOf(id)));
            }
            partLayers.put(part, layers);
        }
    }

    /**
     * @return parte -> (categoria -> modelId resuelto para la seleccion actual)
     */
    public Map<String, Map<String, String>> resolveAll(SkinState state) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        for (var entry : partLayers.entrySet()) {
            String part = entry.getKey();
            Map<String, String> resolvedLayers = new LinkedHashMap<>();
            for (Layer layer : entry.getValue()) {
                String selection = state.getSelection(layer.category());
                String modelId = layer.idTemplate().replace("{" + layer.category() + "}", selection);
                resolvedLayers.put(layer.category(), modelId);
            }
            result.put(part, resolvedLayers);
        }
        return result;
    }
}