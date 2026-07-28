package com.blwardrobe.resourcepack;

import com.blwardrobe.BLWardrobePlugin;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ResourcePackGenerator {

    // Prefijo dentro del jar: los assets terminados van en
    // src/main/resources/resourcepack/... del proyecto Maven.
    private static final String BUNDLED_PREFIX = "resourcepack/";

    private final BLWardrobePlugin plugin;
    private final File bundledDir;      // plugins/BLWardrobe/resourcepack (editable, no se pisa)
    private final File craftEngineDir;  // plugins/CraftEngine
    private final File targetDir;       // plugins/CraftEngine/resources/BLWardrobe/resourcepack

    public ResourcePackGenerator(BLWardrobePlugin plugin) {
        this.plugin = plugin;
        this.bundledDir = new File(plugin.getDataFolder(), "resourcepack");
        this.craftEngineDir = new File(plugin.getDataFolder().getParentFile(), "CraftEngine");
        this.targetDir = new File(craftEngineDir, "resources/BLWardrobe/resourcepack");
    }

    /**
     * Extrae del jar (una sola vez) los assets embebidos en src/main/resources/resourcepack/
     * hacia plugins/BLWardrobe/resourcepack/. Nunca sobrescribe archivos que ya existan ahi,
     * para no pisar ediciones que hagas directamente en el servidor.
     * Llamar en onEnable().
     */
    public void ensureBundledResourcesExtracted() {
        bundledDir.mkdirs();

        try (JarFile jar = new JarFile(getJarFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            int extracted = 0;
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.startsWith(BUNDLED_PREFIX)) continue;

                String relative = name.substring(BUNDLED_PREFIX.length());
                if (relative.isEmpty()) continue;

                File outFile = new File(bundledDir, relative);
                if (outFile.exists()) continue; // no pisar cambios locales

                outFile.getParentFile().mkdirs();
                try (InputStream in = jar.getInputStream(entry);
                     OutputStream out = new FileOutputStream(outFile)) {
                    in.transferTo(out);
                }
                extracted++;
            }
            if (extracted > 0) {
                plugin.getLogger().info("Se extrajeron " + extracted + " archivos de resourcepack a " + bundledDir.getAbsolutePath());
            }
        } catch (IOException | URISyntaxException e) {
            plugin.getLogger().warning("No se pudieron extraer los assets embebidos: " + e.getMessage());
        }
    }

    private File getJarFile() throws URISyntaxException {
        return new File(plugin.getClass()
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
    }

    /**
     * Copia (recursivamente, como carpetas — sin zippear) los assets ya presentes en
     * plugins/BLWardrobe/resourcepack/ hacia plugins/CraftEngine/resources/BLWardrobe/resourcepack/,
     * y despues genera la configuracion de items combinados para CraftEngine.
     * Se puede correr las veces que quieras (/blw resourcepack generate craftengine)
     * despues de agregar o editar assets/categorias.
     */
    public boolean generateForCraftEngine() {
        if (!craftEngineDir.exists()) {
            plugin.getLogger().warning("CraftEngine no esta instalado. No se encontro: " + craftEngineDir.getAbsolutePath());
            return false;
        }
        if (!bundledDir.exists() || !bundledDir.isDirectory()) {
            plugin.getLogger().warning("No hay assets en " + bundledDir.getAbsolutePath() + " para copiar.");
            return false;
        }

        try {
            copyDirectory(bundledDir.toPath(), targetDir.toPath());
            plugin.getLogger().info("Resource pack copiado a: " + targetDir.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().severe("Error al copiar el resource pack: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        CraftEngineConfigGenerator configGen = new CraftEngineConfigGenerator(plugin, craftEngineDir);
        return configGen.generate();
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path src : (Iterable<Path>) paths::iterator) {
                Path dest = target.resolve(source.relativize(src));
                if (Files.isDirectory(src)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}