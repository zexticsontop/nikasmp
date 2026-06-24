package com.legendarysmp.listeners;

import com.legendarysmp.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Prevents a legendary weapon from being crafted more than once per world.
 *
 * When a weapon IS crafted for the first time:
 *   • Adds it to the "crafted-weapons" config list.
 *   • Broadcasts to all players with an achievement-style message.
 *
 * When a player tries to re-craft one that already exists:
 *   • Cancels the event and tells the player why.
 */
public class CraftingListener implements Listener {

    private final LegendarySMP plugin;
    private final WeaponManager wm;

    public CraftingListener(LegendarySMP plugin) {
        this.plugin = plugin;
        this.wm     = plugin.getWeaponManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getRecipe().getResult();
        WeaponType type  = wm.getWeaponType(result);
        if (type == null) return;   // Not a legendary weapon

        // Check permission
        if (!player.hasPermission("legendarysmp.craft")) {
            event.setCancelled(true);
            player.sendMessage("§c§l[LEGENDARY] §r§cYou don't have permission to craft legendary weapons.");
            return;
        }

        List<String> crafted = plugin.getConfig().getStringList("crafted-weapons");
        boolean alreadyCrafted = crafted.contains(type.id);
        boolean allowRecrafting = plugin.getConfig().getBoolean("allow-recrafting", false);

        if (alreadyCrafted && !allowRecrafting) {
            // ── Block duplicate craft ─────────────────────────────────────────
            event.setCancelled(true);
            player.sendMessage("");
            player.sendMessage("§c§l[LEGENDARY] §r§c" + type.displayName + " §calready exists in this world!");
            player.sendMessage("§7Only §c1§7 copy can exist at a time. Find it. Take it.");
            player.sendMessage("");
            return;
        }

        // ── First craft — mark and broadcast ─────────────────────────────────
        if (!alreadyCrafted) {
            crafted.add(type.id);
            plugin.getConfig().set("crafted-weapons", crafted);
            plugin.saveConfig();
        }

        broadcastForge(player, type);
    }

    // ─── Broadcast ─────────────────────────────────────────────────────────────

    private void broadcastForge(Player player, WeaponType type) {
        String line = "§8" + "─".repeat(50);
        Bukkit.broadcastMessage(line);
        Bukkit.broadcastMessage("  §6§l⚒ LEGENDARY WEAPON FORGED ⚒");
        Bukkit.broadcastMessage("  §e" + player.getName() + " §7has crafted:");
        Bukkit.broadcastMessage("  " + type.displayName);
        Bukkit.broadcastMessage("  §7Only §c1 §7copy exists in this world.");
        Bukkit.broadcastMessage(line);

        // Play achievement sound for everyone
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0f, 1.0f);
        }
    }
}
