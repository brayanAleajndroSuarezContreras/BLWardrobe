package com.blwardrobe.storage;

import com.blwardrobe.BLWardrobePlugin;

public class StorageManager {
    private final BLWardrobePlugin plugin;

    public StorageManager(BLWardrobePlugin plugin) {
        this.plugin = plugin;
    }

    public void shutdown() {
        // Guardar datos pendientes
    }
}