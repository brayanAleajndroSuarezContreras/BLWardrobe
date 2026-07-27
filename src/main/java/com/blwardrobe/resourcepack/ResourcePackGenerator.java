package com.blwardrobe.resourcepack;

import com.blwardrobe.BLWardrobePlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ResourcePackGenerator {

    private final BLWardrobePlugin plugin;
    private final File craftEngineDir;
    private final File targetDir;

    public ResourcePackGenerator(BLWardrobePlugin plugin) {
        this.plugin = plugin;
        this.craftEngineDir = new File(JavaPlugin.getPlugin(BLWardrobePlugin.class).getDataFolder().getParentFile(), "CraftEngine");
        this.targetDir = new File(craftEngineDir, "resources/blwardrobe");
    }

    public boolean generateForCraftEngine() {
        if (!craftEngineDir.exists()) {
            plugin.getLogger().warning("CraftEngine no está instalado. No se encontró: " + craftEngineDir.getAbsolutePath());
            return false;
        }

        try {
            createDirs();
            writeModels();
            writeTexturePlaceholders();
            plugin.getLogger().info("Resource pack generado en: " + targetDir.getAbsolutePath());
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error al generar resource pack: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void createDirs() {
        new File(targetDir, "models/item/mannequin").mkdirs();
        new File(targetDir, "textures/item/mannequin/skin").mkdirs();
        new File(targetDir, "textures/item/mannequin/face").mkdirs();
        new File(targetDir, "textures/item/mannequin/shirt").mkdirs();
        new File(targetDir, "textures/item/mannequin/pants").mkdirs();
        new File(targetDir, "textures/item/mannequin/shoes").mkdirs();
        new File(targetDir, "textures/item/mannequin/accessories").mkdirs();
    }

    private void writeModels() throws IOException {
        writeJson("models/item/mannequin/head_default.json", HEAD_JSON);
        writeJson("models/item/mannequin/body_default.json", BODY_JSON);
        writeJson("models/item/mannequin/arm_left_default.json", ARM_LEFT_JSON);
        writeJson("models/item/mannequin/arm_right_default.json", ARM_RIGHT_JSON);
        writeJson("models/item/mannequin/leg_left_default.json", LEG_LEFT_JSON);
        writeJson("models/item/mannequin/leg_right_default.json", LEG_RIGHT_JSON);
    }

    private void writeTexturePlaceholders() throws IOException {
        writeFile("textures/item/mannequin/skin/README.txt",
            "Coloca aquí los tonos de piel (ej: tone_default.png, tone_1.png, etc.)\n" +
            "Formato: 64x64 o recortes de skin de Minecraft.\n");
        writeFile("textures/item/mannequin/face/README.txt",
            "Coloca aquí los rostros (ej: face_default.png, face_1.png, etc.)\n");
        writeFile("textures/item/mannequin/shirt/README.txt",
            "Coloca aquí las camisas (ej: shirt_none.png, shirt_1.png, etc.)\n");
        writeFile("textures/item/mannequin/pants/README.txt",
            "Coloca aquí los pantalones (ej: pants_none.png, pants_1.png, etc.)\n");
        writeFile("textures/item/mannequin/shoes/README.txt",
            "Coloca aquí los zapatos (ej: shoes_none.png, shoes_1.png, etc.)\n");
        writeFile("textures/item/mannequin/accessories/README.txt",
            "Coloca aquí los accesorios (ej: accessories_none.png, accessories_glasses.png, etc.)\n");
    }

    private void writeJson(String relativePath, String content) throws IOException {
        File file = new File(targetDir, relativePath);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    private void writeFile(String relativePath, String content) throws IOException {
        File file = new File(targetDir, relativePath);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    // ─── MODELOS JSON ───


    private static final String HEAD_JSON = "{" +
        "\"credit\": \"BLWardrobe\"," +
        "\"textures\": {" +
        "  \"skin\": \"blwardrobe:item/mannequin/skin/tone_default\"," +
        "  \"face\": \"blwardrobe:item/mannequin/face/face_default\"" +
        "}," +
        "\"elements\": [" +
        "  {" +
        "    \"from\": [-4, 24, -4]," +
        "    \"to\": [4, 32, 4]," +
        "    \"faces\": {" +
        "      \"north\": {\"uv\": [2, 2, 4, 4], \"texture\": \"#face\"}," +
        "      \"east\":  {\"uv\": [0, 2, 2, 4], \"texture\": \"#skin\"}," +
        "      \"south\": {\"uv\": [6, 2, 8, 4], \"texture\": \"#skin\"}," +
        "      \"west\":  {\"uv\": [4, 2, 6, 4], \"texture\": \"#skin\"}," +
        "      \"up\":    {\"uv\": [2, 0, 4, 2], \"texture\": \"#skin\"}," +
        "      \"down\":  {\"uv\": [4, 0, 6, 2], \"texture\": \"#skin\"}" +
        "    }" +
        "  }" +
        "]," +
        "\"display\": {" +
        "  \"head\": {\"translation\": [0, -29, 0], \"scale\": [1.6, 1.6, 1.6]}," +
        "  \"thirdperson_righthand\": {\"scale\": [0.5, 0.5, 0.5]}" +
        "}" +
        "}";

    private static final String BODY_JSON = "{" +
        "\"credit\": \"BLWardrobe\"," +
        "\"textures\": {" +
        "  \"skin\": \"blwardrobe:item/mannequin/skin/tone_default\"," +
        "  \"shirt\": \"blwardrobe:item/mannequin/shirt/shirt_none\"" +
        "}," +
        "\"elements\": [" +
        "  {" +
        "    \"from\": [-4, 12, -2]," +
        "    \"to\": [4, 24, 2]," +
        "    \"faces\": {" +
        "      \"north\": {\"uv\": [5, 5, 7, 8], \"texture\": \"#shirt\"}," +
        "      \"east\":  {\"uv\": [4, 5, 5, 8], \"texture\": \"#skin\"}," +
        "      \"south\": {\"uv\": [8, 5, 10, 8], \"texture\": \"#skin\"}," +
        "      \"west\":  {\"uv\": [7, 5, 8, 8], \"texture\": \"#skin\"}," +
        "      \"up\":    {\"uv\": [5, 4, 7, 5], \"texture\": \"#skin\"}," +
        "      \"down\":  {\"uv\": [7, 4, 9, 5], \"texture\": \"#skin\"}" +
        "    }" +
        "  }" +
        "]," +
        "\"display\": {" +
        "  \"head\": {\"translation\": [0, -18, 0], \"scale\": [1.2, 1.2, 1.2]}" +
        "}" +
        "}";

    private static final String ARM_LEFT_JSON = "{" +
        "\"credit\": \"BLWardrobe\"," +
        "\"textures\": {" +
        "  \"skin\": \"blwardrobe:item/mannequin/skin/tone_default\"" +
        "}," +
        "\"elements\": [" +
        "  {" +
        "    \"from\": [-8, 12, -2]," +
        "    \"to\": [-4, 24, 2]," +
        "    \"faces\": {" +
        "      \"north\": {\"uv\": [11, 5, 12, 8], \"texture\": \"#skin\"}," +
        "      \"east\":  {\"uv\": [10, 5, 11, 8], \"texture\": \"#skin\"}," +
        "      \"south\": {\"uv\": [13, 5, 14, 8], \"texture\": \"#skin\"}," +
        "      \"west\":  {\"uv\": [12, 5, 13, 8], \"texture\": \"#skin\"}," +
        "      \"up\":    {\"uv\": [11, 4, 12, 5], \"texture\": \"#skin\"}," +
        "      \"down\":  {\"uv\": [12, 4, 13, 5], \"texture\": \"#skin\"}" +
        "    }" +
        "  }" +
        "]," +
        "\"display\": {" +
        "  \"head\": {\"translation\": [2, -18, 0], \"scale\": [1.2, 1.2, 1.2]}" +
        "}" +
        "}";

    private static final String ARM_RIGHT_JSON = "{" +
        "\"credit\": \"BLWardrobe\"," +
        "\"textures\": {" +
        "  \"skin\": \"blwardrobe:item/mannequin/skin/tone_default\"" +
        "}," +
        "\"elements\": [" +
        "  {" +
        "    \"from\": [4, 12, -2]," +
        "    \"to\": [8, 24, 2]," +
        "    \"faces\": {" +
        "      \"north\": {\"uv\": [11, 5, 12, 8], \"texture\": \"#skin\"}," +
        "      \"east\":  {\"uv\": [10, 5, 11, 8], \"texture\": \"#skin\"}," +
        "      \"south\": {\"uv\": [13, 5, 14, 8], \"texture\": \"#skin\"}," +
        "      \"west\":  {\"uv\": [12, 5, 13, 8], \"texture\": \"#skin\"}," +
        "      \"up\":    {\"uv\": [11, 4, 12, 5], \"texture\": \"#skin\"}," +
        "      \"down\":  {\"uv\": [12, 4, 13, 5], \"texture\": \"#skin\"}" +
        "    }" +
        "  }" +
        "]," +
        "\"display\": {" +
        "  \"head\": {\"translation\": [-2, -18, 0], \"scale\": [1.2, 1.2, 1.2]}" +
        "}" +
        "}";

    private static final String LEG_LEFT_JSON = "{" +
        "\"credit\": \"BLWardrobe\"," +
        "\"textures\": {" +
        "  \"skin\": \"blwardrobe:item/mannequin/skin/tone_default\"," +
        "  \"pants\": \"blwardrobe:item/mannequin/pants/pants_none\"" +
        "}," +
        "\"elements\": [" +
        "  {" +
        "    \"from\": [-3.9, 0, -2]," +
        "    \"to\": [0.1, 12, 2]," +
        "    \"faces\": {" +
        "      \"north\": {\"uv\": [1, 5, 2, 8], \"texture\": \"#pants\"}," +
        "      \"east\":  {\"uv\": [0, 5, 1, 8], \"texture\": \"#skin\"}," +
        "      \"south\": {\"uv\": [3, 5, 4, 8], \"texture\": \"#skin\"}," +
        "      \"west\":  {\"uv\": [2, 5, 3, 8], \"texture\": \"#skin\"}," +
        "      \"up\":    {\"uv\": [1, 4, 2, 5], \"texture\": \"#skin\"}," +
        "      \"down\":  {\"uv\": [2, 4, 3, 5], \"texture\": \"#skin\"}" +
        "    }" +
        "  }" +
        "]," +
        "\"display\": {" +
        "  \"head\": {\"translation\": [1, -12, 0], \"scale\": [1.2, 1.2, 1.2]}" +
        "}" +
        "}";

    private static final String LEG_RIGHT_JSON = "{" +
        "\"credit\": \"BLWardrobe\"," +
        "\"textures\": {" +
        "  \"skin\": \"blwardrobe:item/mannequin/skin/tone_default\"," +
        "  \"pants\": \"blwardrobe:item/mannequin/pants/pants_none\"" +
        "}," +
        "\"elements\": [" +
        "  {" +
        "    \"from\": [-0.1, 0, -2]," +
        "    \"to\": [3.9, 12, 2]," +
        "    \"faces\": {" +
        "      \"north\": {\"uv\": [1, 5, 2, 8], \"texture\": \"#pants\"}," +
        "      \"east\":  {\"uv\": [0, 5, 1, 8], \"texture\": \"#skin\"}," +
        "      \"south\": {\"uv\": [3, 5, 4, 8], \"texture\": \"#skin\"}," +
        "      \"west\":  {\"uv\": [2, 5, 3, 8], \"texture\": \"#skin\"}," +
        "      \"up\":    {\"uv\": [1, 4, 2, 5], \"texture\": \"#skin\"}," +
        "      \"down\":  {\"uv\": [2, 4, 3, 5], \"texture\": \"#skin\"}" +
        "    }" +
        "  }" +
        "]," +
        "\"display\": {" +
        "  \"head\": {\"translation\": [-1, -12, 0], \"scale\": [1.2, 1.2, 1.2]}" +
        "}" +
        "}";
}