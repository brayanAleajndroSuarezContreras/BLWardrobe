package com.blwardrobe.wardrobe.menu;

import com.blwardrobe.BLWardrobePlugin;
import com.blwardrobe.wardrobe.WardrobeSession;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class WardrobeMenu {
    private final BLWardrobePlugin plugin;
    private final Player player;
    private final WardrobeSession session;
    private final List<Category> categories;
    private final List<ItemDisplay> holograms = new ArrayList<>();
    private int activeCategory = 0;
    private final Location center;

    public WardrobeMenu(BLWardrobePlugin plugin, Player player, WardrobeSession session, Location center) {
        this.plugin = plugin;
        this.player = player;
        this.session = session;
        this.center = center.clone();
        this.categories = loadCategories();
    }

    private List<Category> loadCategories() {
        List<Category> list = new ArrayList<>();
        var configs = plugin.getConfigManager().getCategoryConfigs();
        for (var entry : configs.entrySet()) {
            list.add(new Category(entry.getKey(), entry.getValue()));
        }
        list.sort(Comparator.comparingInt(Category::getSlot));
        return list;
    }

    public void show() {
        spawnHolograms();
        recalculateParts();
        updateVisuals();
        sendControls();
    }

    public void hide() {
        for (ItemDisplay hd : holograms) {
            if (hd != null) hd.remove();
        }
        holograms.clear();
    }

    private void spawnHolograms() {
        double radius = 2.5;
        int count = categories.size();
        for (int i = 0; i < count; i++) {
            final int index = i;
            double angle = (2 * Math.PI * i) / count;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            double y = center.getY() + 2.5;

            Location loc = new Location(center.getWorld(), x, y, z);
            ItemDisplay hd = loc.getWorld().spawn(loc, ItemDisplay.class, display -> {
                display.setItemStack(createIcon(categories.get(index).getIcon()));
                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf(),
                        new Vector3f(1.2f, 1.2f, 1.2f),
                        new Quaternionf()
                ));
                display.setInterpolationDuration(5);
                display.setInterpolationDelay(0);
            });
            holograms.add(hd);
        }
    }

    public void navigateUp() {
        activeCategory = (activeCategory - 1 + categories.size()) % categories.size();
        updateVisuals();
    }

    public void navigateDown() {
        activeCategory = (activeCategory + 1) % categories.size();
        updateVisuals();
    }

    public void navigateLeft() {
        Category cat = categories.get(activeCategory);
        cat.previous();
        updateSkinState(cat);
        recalculateParts();
        updateVisuals();
    }

    public void navigateRight() {
        Category cat = categories.get(activeCategory);
        cat.next();
        updateSkinState(cat);
        recalculateParts();
        updateVisuals();
    }

    private void updateSkinState(Category cat) {
        WardrobeItem item = cat.getSelected();
        if (item != null) {
            session.getSkinState().select(cat.getId(), item.getId());
        }
    }

    private void recalculateParts() {
        var resolver = new com.blwardrobe.wardrobe.skin.PartModelResolver(plugin);
        var models = resolver.resolveAll(session.getSkinState());
        for (var entry : models.entrySet()) {
            session.getMannequin().setPart(entry.getKey(), entry.getValue());
        }
    }

    public void select() {
        Category cat = categories.get(activeCategory);
        WardrobeItem item = cat.getSelected();
        if (item == null) return;

        if (!player.hasPermission(item.getPermission()) && !item.isDefault()) {
            player.sendMessage(color(plugin.getConfig().getString("messages.no-permission", "&cNo tienes permiso.")));
            return;
        }

        player.sendMessage(color("&aSeleccionado: &f" + item.getName()));
        player.sendMessage(color("&7Pulsa &fF &7de nuevo para guardar y aplicar."));
    }

    public void saveSkin() {
        player.sendMessage(color("&aGenerando skin..."));
        // TODO: SkinComposer + SkinUploader
    }

    private void updateVisuals() {
        for (int i = 0; i < holograms.size(); i++) {
            ItemDisplay hd = holograms.get(i);
            if (hd == null) continue;

            boolean active = (i == activeCategory);
            Vector3f scale = active ? new Vector3f(1.6f, 1.6f, 1.6f) : new Vector3f(1.0f, 1.0f, 1.0f);

            hd.setTransformation(new Transformation(
                    new Vector3f(0, active ? 0.3f : 0, 0),
                    new Quaternionf(),
                    scale,
                    new Quaternionf()
            ));

            Category cat = categories.get(i);
            WardrobeItem item = cat.getSelected();
            if (item != null) {
                hd.setItemStack(createIcon(cat.getIcon()));
            }

            if (active) {
                hd.setGlowColorOverride(org.bukkit.Color.YELLOW);
                hd.setGlowing(true);
            } else {
                hd.setGlowing(false);
            }
        }

        Category active = categories.get(activeCategory);
        WardrobeItem item = active.getSelected();
        String msg = color("&e" + active.getName() + " &7| &f" + (item != null ? item.getName() : "???"));
        player.sendActionBar(msg);
    }

    private ItemStack createIcon(String materialName) {
        Material mat = Material.matchMaterial(materialName);
        if (mat == null) mat = Material.PAPER;
        return new ItemStack(mat);
    }

    private void sendControls() {
        player.sendMessage(color("&7&lControles del Armario:"));
        player.sendMessage(color("&7W/S &f- Cambiar categoria"));
        player.sendMessage(color("&7A/D &f- Cambiar prenda"));
        player.sendMessage(color("&7F &f- Aplicar / Guardar"));
        player.sendMessage(color("&7SHIFT &f- Salir"));
    }

    private String color(String msg) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }
}