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

public class Mannequin {
    private final BLWardrobePlugin plugin;
    private ItemDisplay head, body, leftArm, rightArm, leftLeg, rightLeg;
    private Location center;

    public Mannequin(BLWardrobePlugin plugin) {
        this.plugin = plugin;
    }

    public void spawn(Location center) {
        this.center = center.clone();
        float yaw = center.getYaw();

        head = spawnPart(offset(center, 0, 1.5, 0, yaw), "mannequin/head_default", new Vector3f(1f, 1f, 1f), yaw);
        body = spawnPart(offset(center, 0, 0.75, 0, yaw), "mannequin/body_default", new Vector3f(1f, 1.5f, 0.5f), yaw);
        leftArm = spawnPart(offset(center, -0.6, 0.75, 0, yaw), "mannequin/arm_left_default", new Vector3f(0.5f, 1.5f, 0.5f), yaw);
        rightArm = spawnPart(offset(center, 0.6, 0.75, 0, yaw), "mannequin/arm_right_default", new Vector3f(0.5f, 1.5f, 0.5f), yaw);
        leftLeg = spawnPart(offset(center, -0.25, -0.25, 0, yaw), "mannequin/leg_left_default", new Vector3f(0.5f, 1.5f, 0.5f), yaw);
        rightLeg = spawnPart(offset(center, 0.25, -0.25, 0, yaw), "mannequin/leg_right_default", new Vector3f(0.5f, 1.5f, 0.5f), yaw);
    }

    private Location offset(Location base, double dx, double dy, double dz, float yaw) {
        double rad = Math.toRadians(-yaw);
        double x = base.getX() + (dx * Math.cos(rad) - dz * Math.sin(rad));
        double z = base.getZ() + (dx * Math.sin(rad) + dz * Math.cos(rad));
        return new Location(base.getWorld(), x, base.getY() + dy, z, yaw, 0f);
    }

    private ItemDisplay spawnPart(Location loc, String modelPath, Vector3f scale, float yaw) {
        return loc.getWorld().spawn(loc, ItemDisplay.class, display -> {
            display.setItemStack(createModelItem(modelPath));
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

    public void setPart(String part, String modelId) {
        ItemDisplay target = switch (part.toLowerCase()) {
            case "head", "face" -> head;
            case "body", "shirt" -> body;
            case "leftarm" -> leftArm;
            case "rightarm" -> rightArm;
            case "leftleg", "pants" -> leftLeg;
            case "rightleg" -> rightLeg;
            default -> null;
        };
        if (target != null) {
            target.setItemStack(createModelItem(modelId));
        }
    }

    public void despawn() {
        if (head != null) head.remove();
        if (body != null) body.remove();
        if (leftArm != null) leftArm.remove();
        if (rightArm != null) rightArm.remove();
        if (leftLeg != null) leftLeg.remove();
        if (rightLeg != null) rightLeg.remove();
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
        item.editMeta(meta -> {
            meta.setItemModel(new NamespacedKey(namespace, key));
        });
        return item;
    }
}