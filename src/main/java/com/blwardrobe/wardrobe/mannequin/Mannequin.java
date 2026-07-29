package com.blwardrobe.wardrobe.mannequin;

import com.blwardrobe.BLWardrobePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Mannequin {

    private record PartGeometry(double dx, double dy, double dz, Vector3f scale) {}

    // Geometria de cada parte del cuerpo (igual que antes). Todas las capas
    // de una misma parte comparten esta posicion/escala base.
    private static final Map<String, PartGeometry> PART_GEOMETRY = Map.of(
            "head", new PartGeometry(0, 1.5, 0, new Vector3f(1f, 1f, 1f)),
            "body", new PartGeometry(0, 0.75, 0, new Vector3f(1f, 1.5f, 0.5f)),
            "leftarm", new PartGeometry(0.6, 0.75, 0, new Vector3f(0.5f, 1.5f, 0.5f)),
            "rightarm", new PartGeometry(-0.6, 0.75, 0, new Vector3f(0.5f, 1.5f, 0.5f)),
            "leftleg", new PartGeometry(0.25, -0.25, 0, new Vector3f(0.5f, 1.5f, 0.5f)),
            "rightleg", new PartGeometry(-0.25, -0.25, 0, new Vector3f(0.5f, 1.5f, 0.5f))
    );

    // Pequenio corrimiento hacia adelante por capa para evitar z-fighting entre
    // ItemDisplay superpuestos en el mismo lugar (skin al fondo, ropa encima).
    // Si en el juego se ve raro (huecos, parpadeo), este es el primer valor a tocar.
    private static final double LAYER_STEP = 0.00;

    private final BLWardrobePlugin plugin;
    private Location center;

    // clave "parte:categoria" -> el ItemDisplay que representa esa capa
    private final Map<String, ItemDisplay> layers = new LinkedHashMap<>();

    public Mannequin(BLWardrobePlugin plugin) {
        this.plugin = plugin;
    }

    public void spawn(Location center) {
        this.center = center.clone();
        float yaw = center.getYaw();

        var partsSection = plugin.getConfig().getConfigurationSection("mannequin-layers.parts");
        if (partsSection == null) {
            plugin.getLogger().warning("Falta 'mannequin-layers.parts' en config.yml, el maniquin no tiene capas que spawnear.");
            return;
        }

        for (String part : partsSection.getKeys(false)) {
            PartGeometry geo = PART_GEOMETRY.get(part);
            if (geo == null) continue;

            List<Map<?, ?>> layerConfigs = partsSection.getMapList(part);
            for (int i = 0; i < layerConfigs.size(); i++) {
                Object categoryRaw = layerConfigs.get(i).get("category");
                if (categoryRaw == null) continue;
                String category = String.valueOf(categoryRaw);

                double forward = i * LAYER_STEP; // capas siguientes un poco mas afuera
                Location loc = offset(center, geo.dx(), geo.dy(), geo.dz() + forward, yaw);
                ItemDisplay display = spawnLayer(loc, geo.scale(), yaw);
                layers.put(key(part, category), display);
            }
        }
    }

    private Location offset(Location base, double dx, double dy, double dz, float yaw) {
        double rad = Math.toRadians(-yaw);
        double x = base.getX() + (dx * Math.cos(rad) - dz * Math.sin(rad));
        double z = base.getZ() + (dx * Math.sin(rad) + dz * Math.cos(rad));
        return new Location(base.getWorld(), x, base.getY() + dy, z, yaw, 0f);
    }

    private ItemDisplay spawnLayer(Location loc, Vector3f scale, float yaw) {
        return loc.getWorld().spawn(loc, ItemDisplay.class, display -> {
            Quaternionf rotation = new Quaternionf().rotateY((float) Math.toRadians(-yaw));
            display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    rotation,
                    scale,
                    new Quaternionf()
            ));
            display.setInterpolationDuration(0);
            display.setInterpolationDelay(0);
        });
    }

    /**
     * Actualiza UNA capa especifica (parte + categoria) con el modelo que
     * corresponde a la seleccion actual de esa categoria. El resto de las
     * capas de esa misma parte no se tocan.
     */
    public void setLayer(String part, String category, String modelId) {
        ItemDisplay target = layers.get(key(part, category));
        if (target == null) return;

        target.setItemStack(createModelItem(modelId));

        // Forma basica, sin estirar (ver conversacion previa sobre esto).
        Transformation current = target.getTransformation();
        target.setTransformation(new Transformation(
                current.getTranslation(),
                current.getLeftRotation(),
                new Vector3f(1f, 1f, 1f),
                current.getRightRotation()
        ));
    }

    private String key(String part, String category) {
        return part + ":" + category;
    }

    public void despawn() {
        for (ItemDisplay display : layers.values()) {
            if (display != null) display.remove();
        }
        layers.clear();
    }

    private ItemStack createModelItem(String modelPath) {
        final String namespace;
        final String key;

        if (modelPath.contains(":")) {
            String[] parts = modelPath.split(":", 2);
            namespace = parts[0];
            key = parts[1];
        } else {
            namespace = "blwardrobe";
            key = modelPath;
        }

        ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> meta.setItemModel(new NamespacedKey(namespace, key)));
        return item;
    }
}