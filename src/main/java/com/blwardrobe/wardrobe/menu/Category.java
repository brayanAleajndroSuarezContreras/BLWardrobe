package com.blwardrobe.wardrobe.menu;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class Category {
    private final String id;
    private final String name;
    private final int slot;
    private final String icon;
    private final List<WardrobeItem> items;
    private int selectedIndex = 0;

    public Category(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name", id);
        this.slot = section.getInt("slot", 0);
        this.icon = section.getString("icon", "PAPER");
        this.items = new ArrayList<>();

        var itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemId : itemsSection.getKeys(false)) {
                var itemSec = itemsSection.getConfigurationSection(itemId);
                if (itemSec != null) {
                    items.add(new WardrobeItem(itemId, itemSec));
                }
            }
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getSlot() { return slot; }
    public String getIcon() { return icon; }
    public List<WardrobeItem> getItems() { return items; }

    public WardrobeItem getSelected() {
        if (items.isEmpty()) return null;
        return items.get(selectedIndex);
    }

    public void next() {
        selectedIndex = (selectedIndex + 1) % items.size();
    }

    public void previous() {
        selectedIndex = (selectedIndex - 1 + items.size()) % items.size();
    }

    public void setIndex(int index) {
        if (index >= 0 && index < items.size()) selectedIndex = index;
    }
}