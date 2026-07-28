package com.blwardrobe.command;

import com.blwardrobe.BLWardrobePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BLWCommand implements CommandExecutor, TabCompleter {
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
                plugin.getConfigManager().loadAll();
                sender.sendMessage(color("&aConfiguración y categorías recargadas."));
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
                    boolean ok = plugin.getResourcePackGenerator().generateForCraftEngine();
                    if (ok) {
                        sender.sendMessage(color("&aResource pack copiado a plugins/CraftEngine/resources/BLWardrobe/resourcepack/"));
                    } else {
                        sender.sendMessage(color("&cNo se pudo copiar. ¿CraftEngine está instalado y hay assets en plugins/BLWardrobe/resourcepack/?"));
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("blwardrobe.use")) subs.add("open");
            if (sender.hasPermission("blwardrobe.admin")) {
                subs.add("reload");
                subs.add("resourcepack");
            }
            StringUtil.copyPartialMatches(args[0], subs, completions);
        } else if (args.length == 2 && isResourcepackSub(args[0]) && sender.hasPermission("blwardrobe.admin")) {
            StringUtil.copyPartialMatches(args[1], List.of("generate"), completions);
        } else if (args.length == 3 && isResourcepackSub(args[0]) && args[1].equalsIgnoreCase("generate")
                && sender.hasPermission("blwardrobe.admin")) {
            StringUtil.copyPartialMatches(args[2], List.of("craftengine"), completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private boolean isResourcepackSub(String arg) {
        return arg.equalsIgnoreCase("resourcepack") || arg.equalsIgnoreCase("rp");
    }

    private String color(String msg) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }
}