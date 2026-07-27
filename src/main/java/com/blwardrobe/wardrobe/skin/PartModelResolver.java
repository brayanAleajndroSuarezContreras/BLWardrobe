package com.blwardrobe.wardrobe.skin;

import com.blwardrobe.BLWardrobePlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class PartModelResolver {
    private final BLWardrobePlugin plugin;
    private final Map<String, String> partTemplates = new HashMap<>();

    public PartModelResolver(BLWardrobePlugin plugin) {
        this.plugin = plugin;
        loadTemplates();
    }

    private void loadTemplates() {
        var section = plugin.getConfig().getConfigurationSection("part-models");
        if (section == null) {
            partTemplates.put("head", "head_{skin}_{face}");
            partTemplates.put("body", "body_{skin}_{shirts}");
            partTemplates.put("leftarm", "arm_left_{skin}_{shirts}");
            partTemplates.put("rightarm", "arm_right_{skin}_{shirts}");
            partTemplates.put("leftleg", "leg_left_{skin}_{pants}");
            partTemplates.put("rightleg", "leg_right_{skin}_{pants}");
            return;
        }
        for (String part : section.getKeys(false)) {
            partTemplates.put(part, section.getString(part));
        }
    }

    public String resolve(String part, SkinState state) {
        String template = partTemplates.getOrDefault(part, part);
        String result = template;
        for (Map.Entry<String, String> entry : state.getAll().entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        result = result.replaceAll("\\{[^}]+\\}", "default");
        return result;
    }

    public Map<String, String> resolveAll(SkinState state) {
        Map<String, String> models = new HashMap<>();
        for (String part : partTemplates.keySet()) {
            models.put(part, resolve(part, state));
        }
        return models;
    }
}