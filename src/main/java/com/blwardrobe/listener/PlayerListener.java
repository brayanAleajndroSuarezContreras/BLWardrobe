package com.blwardrobe.listener;

import com.blwardrobe.BLWardrobePlugin;
import com.blwardrobe.wardrobe.WardrobeSession;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final BLWardrobePlugin plugin;
    private final Map<UUID, Long> lastMove = new HashMap<>();
    private static final long COOLDOWN_MS = 200;

    public PlayerListener(BLWardrobePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        WardrobeSession session = plugin.getWardrobeManager().getSession(player);
        if (session == null) return;

        event.setCancelled(true);

        long now = System.currentTimeMillis();
        Long last = lastMove.get(player.getUniqueId());
        if (last != null && (now - last) < COOLDOWN_MS) return;
        lastMove.put(player.getUniqueId(), now);

        Location from = event.getFrom();
        Location to = event.getTo();

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        if (Math.abs(dx) < 0.001 && Math.abs(dz) < 0.001) return;

        double yaw = Math.toRadians(player.getLocation().getYaw());
        double forward = -dx * Math.sin(yaw) + dz * Math.cos(yaw);
        double strafe = dx * Math.cos(yaw) + dz * Math.sin(yaw);

        if (Math.abs(forward) > Math.abs(strafe)) {
            if (forward > 0.01) session.getMenu().navigateRight();   // W = siguiente prenda
            else if (forward < -0.01) session.getMenu().navigateLeft(); // S = prenda anterior
        } else {
            if (strafe > 0.01) session.getMenu().navigateUp();       // A = categoria izquierda
            else if (strafe < -0.01) session.getMenu().navigateDown(); // D = categoria derecha
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking() && plugin.getWardrobeManager().isInWardrobe(event.getPlayer())) {
            lastMove.remove(event.getPlayer().getUniqueId());
            plugin.getWardrobeManager().closeWardrobe(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (plugin.getWardrobeManager().isInWardrobe(event.getPlayer())) {
            event.setCancelled(true);
            WardrobeSession session = plugin.getWardrobeManager().getSession(event.getPlayer());
            if (session != null && session.getMenu() != null) {
                session.getMenu().select();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastMove.remove(event.getPlayer().getUniqueId());
        if (plugin.getWardrobeManager().isInWardrobe(event.getPlayer())) {
            plugin.getWardrobeManager().closeWardrobe(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (plugin.getWardrobeManager().isInWardrobe(event.getPlayer())) {
            if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (plugin.getWardrobeManager().isInWardrobe(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (plugin.getWardrobeManager().isInWardrobe(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}