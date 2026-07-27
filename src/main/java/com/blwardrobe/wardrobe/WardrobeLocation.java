package com.blwardrobe.wardrobe;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

public class WardrobeLocation {
    private final String world;
    private final double mannequinX, mannequinY, mannequinZ;
    private final BlockFace orientation;
    private final double cameraX, cameraY, cameraZ;
    private final float cameraYaw, cameraPitch;

    public WardrobeLocation(String world,
                            double mannequinX, double mannequinY, double mannequinZ, String orientation,
                            double cameraX, double cameraY, double cameraZ, float cameraYaw, float cameraPitch) {
        this.world = world;
        this.mannequinX = mannequinX;
        this.mannequinY = mannequinY;
        this.mannequinZ = mannequinZ;
        this.orientation = parseOrientation(orientation);
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
        this.cameraYaw = cameraYaw;
        this.cameraPitch = cameraPitch;
    }

    private BlockFace parseOrientation(String orient) {
        return switch (orient.toUpperCase()) {
            case "NORTH" -> BlockFace.NORTH;
            case "SOUTH" -> BlockFace.SOUTH;
            case "EAST" -> BlockFace.EAST;
            case "WEST" -> BlockFace.WEST;
            default -> BlockFace.SOUTH;
        };
    }

    public Location getMannequinLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) w = Bukkit.getWorlds().get(0);
        float yaw = switch (orientation) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case EAST -> -90f;
            case WEST -> 90f;
            default -> 0f;
        };
        return new Location(w, mannequinX, mannequinY, mannequinZ, yaw, 0f);
    }

    public Location getCameraLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) w = Bukkit.getWorlds().get(0);
        return new Location(w, cameraX, cameraY, cameraZ, cameraYaw, cameraPitch);
    }
}