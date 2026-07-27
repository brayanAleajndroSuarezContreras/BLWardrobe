package com.blwardrobe.wardrobe;

import com.blwardrobe.BLWardrobePlugin;
import com.blwardrobe.wardrobe.mannequin.Mannequin;
import com.blwardrobe.wardrobe.menu.WardrobeMenu;
import com.blwardrobe.wardrobe.skin.SkinState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WardrobeSession {
    private final BLWardrobePlugin plugin;
    private final Player player;
    private final WardrobeLocation wardrobeLocation;
    private final Location originalLocation;
    private final GameMode originalGameMode;
    private final Mannequin mannequin;
    private final SkinState skinState;
    private WardrobeMenu menu;
    private boolean valid = true;

    public WardrobeSession(BLWardrobePlugin plugin, Player player, WardrobeLocation wardrobeLocation) {
        this.plugin = plugin;
        this.player = player;
        this.wardrobeLocation = wardrobeLocation;
        this.originalLocation = player.getLocation().clone();
        this.originalGameMode = player.getGameMode();
        this.mannequin = new Mannequin(plugin);
        this.skinState = new SkinState();
    }

    public void open() {
        player.teleport(wardrobeLocation.getCameraLocation());
        player.setGameMode(GameMode.SPECTATOR);
        mannequin.spawn(wardrobeLocation.getMannequinLocation());
        this.menu = new WardrobeMenu(plugin, player, this, wardrobeLocation.getMannequinLocation());
        menu.show();
        player.sendMessage(color(plugin.getConfig().getString("messages.open-wardrobe", "&aAbriendo tu armario!")));
    }

    public void close() {
        if (menu != null) menu.hide();
        mannequin.despawn();
        player.setGameMode(originalGameMode);
        player.teleport(originalLocation);
        player.sendMessage(color(plugin.getConfig().getString("messages.close-wardrobe", "&cCerrando armario...")));
        valid = false;
    }

    public boolean isValid() {
        return valid && player.isOnline();
    }

    public Mannequin getMannequin() { return mannequin; }
    public WardrobeMenu getMenu() { return menu; }
    public SkinState getSkinState() { return skinState; }
    public Player getPlayer() { return player; }

    public void nextCategory() {
        if (menu != null) menu.navigateDown();
    }

    private String color(String msg) {
        return msg == null ? "" : org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }
}