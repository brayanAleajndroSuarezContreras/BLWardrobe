package com.blwardrobe.wardrobe.menu;

import org.bukkit.configuration.ConfigurationSection;

public class WardrobeItem {
    private final String id;
    private final String name;
    private final String permission;
    private final boolean isDefault;
    private final String model;

    public WardrobeItem(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name", id);
        this.permission = section.getString("permission", "");
        this.isDefault = section.getBoolean("default", false);
        this.model = section.getString("model", "");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPermission() { return permission; }
    public boolean isDefault() { return isDefault; }
    public String getModel() { return model; }
}