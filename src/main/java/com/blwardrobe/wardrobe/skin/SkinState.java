package com.blwardrobe.wardrobe.skin;

import java.util.HashMap;
import java.util.Map;

public class SkinState {
    private final Map<String, String> selections = new HashMap<>();

    public void select(String category, String itemId) {
        selections.put(category, itemId);
    }

    public String getSelection(String category) {
        return selections.getOrDefault(category, "default");
    }

    public Map<String, String> getAll() {
        return new HashMap<>(selections);
    }

}