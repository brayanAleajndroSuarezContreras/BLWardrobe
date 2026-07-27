package com.blwardrobe.command;

import com.blwardrobe.BLWardrobePlugin;
import com.blwardrobe.resourcepack.ResourcePackGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BLWCommand implements CommandExecutor {
    private final BLWardrobePlugin plugin;

    public BLWCommand(BLWardrobePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "open" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Solo jugadores.");
                    return true;
                }
                if (!player.hasPermission("blwardrobe.use")) {
                    player.sendMessage(color(plugin.getConfig().getString("messages.no-permission-command", "&cNo tienes permiso.")));
                    return true;
                }
                plugin.getWardrobeManager().openWardrobe(player, "spawn");
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("blwardrobe.admin")) {
                    sender.sendMessage(color("&cNo tienes permiso."));
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(color("&aConfiguración recargada."));
                return true;
            }
            case "resourcepack", "rp" -> {
                if (!sender.hasPermission("blwardrobe.admin")) {
                    sender.sendMessage(color("&cNo tienes permiso."));
                    return true;
                }
                if (args.length < 3 || !args[1].equalsIgnoreCase("generate")) {
                    sender.sendMessage(color("&7Uso: /blw resourcepack generate craftengine"));
                    return true;
                }
                String target = args[2].toLowerCase();
                if (target.equals("craftengine")) {
                    ResourcePackGenerator gen = new ResourcePackGenerator(plugin);
                    boolean ok = gen.generateForCraftEngine();
                    if (ok) {
                        sender.sendMessage(color("&aResource pack generado en plugins/CraftEngine/resources/blwardrobe/"));
                        sender.sendMessage(color("&7Ahora coloca tus texturas PNG en las carpetas correspondientes."));
                    } else {
                        sender.sendMessage(color("&cNo se pudo generar. ¿CraftEngine está instalado?"));
                    }
                } else {
                    sender.sendMessage(color("&cTarget no válido. Usa: craftengine"));
                }
                return true;
            }
            default -> {
                sendHelp(sender);
                return true;
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&6&lBLWardrobe &7- Comandos"));
        sender.sendMessage(color("&7/blw open &f- Abrir armario"));
        sender.sendMessage(color("&7/blw reload &f- Recargar config"));
        sender.sendMessage(color("&7/blw resourcepack generate craftengine &f- Generar assets para CraftEngine"));
    }

    private String color(String msg) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }
}