package com.blwardrobe.wardrobe;

import com.blwardrobe.BLWardrobePlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WardrobeManager {

    private final BLWardrobePlugin plugin;
    private final Map<UUID, WardrobeSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, WardrobeLocation> wardrobeLocations = new HashMap<>();

    public WardrobeManager(BLWardrobePlugin plugin) {
        this.plugin = plugin;
        loadWardrobeLocations();
        startSessionChecker();
    }

    private void loadWardrobeLocations() {
        var section = plugin.getConfig().getConfigurationSection("wardrobes");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            var loc = section.getConfigurationSection(key);
            if (loc == null || !loc.getBoolean("enabled", true)) continue;

            var mannequin = loc.getConfigurationSection("mannequin");
            var camera = loc.getConfigurationSection("camera");

            wardrobeLocations.put(key, new WardrobeLocation(
                loc.getString("world", "world"),
                mannequin != null ? mannequin.getDouble("x", 0) : loc.getDouble("x", 0),
                mannequin != null ? mannequin.getDouble("y", 64) : loc.getDouble("y", 64),
                mannequin != null ? mannequin.getDouble("z", 0) : loc.getDouble("z", 0),
                mannequin != null ? mannequin.getString("orientation", "SOUTH") : "SOUTH",
                camera != null ? camera.getDouble("x", 0) : loc.getDouble("x", 0),
                camera != null ? camera.getDouble("y", 65) : loc.getDouble("y", 65),
                camera != null ? camera.getDouble("z", 3) : loc.getDouble("z", 3),
                camera != null ? (float) camera.getDouble("yaw", 180) : 180f,
                camera != null ? (float) camera.getDouble("pitch", 10) : 10f
            ));
        }
    }

    public void openWardrobe(Player player, String wardrobeId) {
        if (sessions.containsKey(player.getUniqueId())) {
            player.sendMessage(color(plugin.getConfig().getString("messages.wardrobe-full", "&cEl armario esta ocupado.")));
            return;
        }

        WardrobeLocation loc = wardrobeLocations.get(wardrobeId);
        if (loc == null) {
            Location p = player.getLocation();
            loc = new WardrobeLocation(
                p.getWorld().getName(),
                p.getX(), p.getY(), p.getZ(), "SOUTH",
                p.getX(), p.getY(), p.getZ() + 3.0, p.getYaw(), p.getPitch()
            );
        }

        WardrobeSession session = new WardrobeSession(plugin, player, loc);
        sessions.put(player.getUniqueId(), session);
        session.open();
    }

    public void closeWardrobe(Player player) {
        WardrobeSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.close();
        }
    }

    public WardrobeSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public boolean isInWardrobe(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    private void startSessionChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, WardrobeSession>> it = sessions.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, WardrobeSession> entry = it.next();
                    WardrobeSession session = entry.getValue();
                    if (!session.isValid()) {
                        session.close();
                        it.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void shutdown() {
        for (WardrobeSession session : new ArrayList<>(sessions.values())) {
            session.close();
        }
        sessions.clear();
    }

    private String color(String msg) {
        return msg == null ? "" : org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }
}