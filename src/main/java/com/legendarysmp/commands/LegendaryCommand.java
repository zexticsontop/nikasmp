package com.legendarysmp.commands;

import com.legendarysmp.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * /legendary — Admin command for managing legendary items.
 *
 * Sub-commands:
 *   give   <player> <weapon_id>   — Give a legendary weapon to a player
 *   egg    <player>               — Give the Dragon Egg to a player
 *   status                        — Show which weapons have been crafted
 *   reset  <weapon_id|all>        — Allow a weapon to be re-crafted
 *   reload                        — Reload the config file
 */
public class LegendaryCommand implements CommandExecutor, TabCompleter {

    private final LegendarySMP plugin;

    public LegendaryCommand(LegendarySMP plugin) {
        this.plugin = plugin;
    }

    // ─── Command Dispatch ──────────────────────────────────────────────────────

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("legendarysmp.admin")) {
            sender.sendMessage("§c[LegendarySMP] You don't have permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "give"   -> cmdGive(sender, args);
            case "egg"    -> cmdEgg(sender, args);
            case "status" -> cmdStatus(sender);
            case "reset"  -> cmdReset(sender, args);
            case "reload" -> cmdReload(sender);
            default       -> { sendHelp(sender); yield true; }
        };
    }

    // ─── Sub-commands ──────────────────────────────────────────────────────────

    private boolean cmdGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /legendary give <player> <weapon_id>");
            sender.sendMessage("§7Weapon IDs: " + WeaponType.allIds());
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer §e" + args[1] + " §cnot found or offline.");
            return true;
        }

        WeaponType type = WeaponType.fromId(args[2]);
        if (type == null) {
            sender.sendMessage("§cUnknown weapon: §e" + args[2]);
            sender.sendMessage("§7Valid IDs: " + WeaponType.allIds());
            return true;
        }

        ItemStack item = plugin.getWeaponManager().createWeapon(type);
        target.getInventory().addItem(item);
        sender.sendMessage("§aGave " + type.displayName + " §ato §e" + target.getName() + "§a.");
        target.sendMessage("§6[LegendarySMP] §r§eYou received: §r" + type.displayName);
        return true;
    }

    private boolean cmdEgg(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /legendary egg <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer §e" + args[1] + " §cnot found or offline.");
            return true;
        }

        ItemStack egg = plugin.getDragonEggManager().createDragonEgg();
        target.getInventory().addItem(egg);
        sender.sendMessage("§aGave §5Dragon Egg §ato §e" + target.getName() + "§a.");
        target.sendMessage("§5[LegendarySMP] §r§dYou received the §5✦ Dragon Egg ✦§d!");
        return true;
    }

    private boolean cmdStatus(CommandSender sender) {
        List<String> crafted = plugin.getConfig().getStringList("crafted-weapons");
        sender.sendMessage("§8" + "─".repeat(40));
        sender.sendMessage("§6§l  Legendary Weapon Status");
        sender.sendMessage("§8" + "─".repeat(40));
        for (WeaponType type : WeaponType.values()) {
            boolean done = crafted.contains(type.id);
            String status = done ? "§a[EXISTS]" : "§7[CRAFTABLE]";
            sender.sendMessage("  " + status + " §r" + type.displayName + " §8(" + type.id + ")");
        }
        sender.sendMessage("§8" + "─".repeat(40));
        sender.sendMessage("§7Re-crafting allowed: "
            + (plugin.getConfig().getBoolean("allow-recrafting", false) ? "§ayes" : "§cno"));
        return true;
    }

    private boolean cmdReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /legendary reset <weapon_id|all>");
            return true;
        }

        if (args[1].equalsIgnoreCase("all")) {
            plugin.getConfig().set("crafted-weapons", new ArrayList<>());
            plugin.saveConfig();
            sender.sendMessage("§aReset all legendary weapon crafting restrictions.");
            return true;
        }

        WeaponType type = WeaponType.fromId(args[1]);
        if (type == null) {
            sender.sendMessage("§cUnknown weapon: §e" + args[1]);
            return true;
        }

        List<String> crafted = new ArrayList<>(plugin.getConfig().getStringList("crafted-weapons"));
        if (crafted.remove(type.id)) {
            plugin.getConfig().set("crafted-weapons", crafted);
            plugin.saveConfig();
            sender.sendMessage("§aReset crafting restriction for " + type.displayName + "§a.");
        } else {
            sender.sendMessage("§e" + type.displayName + " §7has not been crafted yet — nothing to reset.");
        }
        return true;
    }

    private boolean cmdReload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage("§a[LegendarySMP] Config reloaded.");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l[LegendarySMP] §r§7Commands:");
        sender.sendMessage("§e/legendary give §f<player> <id>   §7— Give a legendary weapon");
        sender.sendMessage("§e/legendary egg  §f<player>        §7— Give the Dragon Egg");
        sender.sendMessage("§e/legendary status                 §7— Show crafting status");
        sender.sendMessage("§e/legendary reset §f<id|all>       §7— Reset crafting restriction");
        sender.sendMessage("§e/legendary reload                 §7— Reload config");
        sender.sendMessage("§7Weapon IDs: " + WeaponType.allIds());
    }

    // ─── Tab Completion ────────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("legendarysmp.admin")) return List.of();

        if (args.length == 1) {
            return List.of("give", "egg", "status", "reset", "reload");
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "give", "egg" -> Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).toList();
                case "reset" -> {
                    List<String> options = new ArrayList<>();
                    options.add("all");
                    for (WeaponType t : WeaponType.values()) options.add(t.id);
                    yield options;
                }
                default -> List.of();
            };
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> ids = new ArrayList<>();
            for (WeaponType t : WeaponType.values()) ids.add(t.id);
            return ids;
        }

        return List.of();
    }
}
