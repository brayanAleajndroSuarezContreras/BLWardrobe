package com.blwardrobe.util;

import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Extrae, desde el jar del propio plugin, todo lo que este bajo un prefijo
 * dado (rutas dentro de src/main/resources/) hacia una carpeta destino en
 * el filesystem, preservando la estructura de subcarpetas.
 *
 * Nunca sobrescribe un archivo que ya exista en el destino, para no pisar
 * ediciones hechas a mano en el servidor.
 */
public final class JarResourceExtractor {

    private JarResourceExtractor() {}

    /**
     * @param jarPrefix carpeta dentro del jar, ej. "resourcepack/" o "categories/skin/"
     * @param targetDir carpeta destino en el filesystem
     * @return cantidad de archivos nuevos extraidos
     */
    public static int extract(Plugin plugin, String jarPrefix, File targetDir) {
        targetDir.mkdirs();
        int extracted = 0;

        try (JarFile jar = new JarFile(getJarFile(plugin))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.startsWith(jarPrefix)) continue;

                String relative = name.substring(jarPrefix.length());
                if (relative.isEmpty()) continue;

                File outFile = new File(targetDir, relative);
                if (outFile.exists()) continue; // no pisar cambios locales

                outFile.getParentFile().mkdirs();
                try (InputStream in = jar.getInputStream(entry);
                     OutputStream out = new FileOutputStream(outFile)) {
                    in.transferTo(out);
                }
                extracted++;
            }
        } catch (IOException | URISyntaxException e) {
            plugin.getLogger().warning("No se pudo extraer '" + jarPrefix + "' del jar: " + e.getMessage());
        }

        return extracted;
    }

    private static File getJarFile(Plugin plugin) throws URISyntaxException {
        return new File(plugin.getClass()
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
    }
}