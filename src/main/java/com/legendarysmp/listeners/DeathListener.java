package com.legendarysmp.listeners;

import com.legendarysmp.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Ensures all legendary weapons and the Dragon Egg always drop on death,
 * regardless of keepInventory gamerule or other plugins.
 *
 * Runs at HIGHEST priority so it acts after most plugins but still enforces drops.
 */
public class DeathListener implements Listener {

    private final LegendarySMP plugin;
    private final WeaponManager wm;
    private final DragonEggManager em;

    public DeathListener(LegendarySMP plugin) {
        this.plugin = plugin;
        this.wm     = plugin.getWeaponManager();
        this.em     = plugin.getDragonEggManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Collect every inventory slot
        ItemStack[] contents = player.getInventory().getContents();
        List<ItemStack> toForce = new ArrayList<>();

        for (ItemStack item : contents) {
            if (item == null) continue;
            if (wm.isLegendary(item) || em.isDragonEgg(item)) {
                toForce.add(item);
            }
        }

        if (toForce.isEmpty()) return;

        for (ItemStack forced : toForce) {
            // Ensure it's in the drop list
            if (!event.getDrops().contains(forced)) {
                event.getDrops().add(forced.clone());
            }
            // Remove from player inventory so keepInventory cannot keep it
            player.getInventory().remove(forced);
        }

        // Build a message summarising what was dropped
        if (!toForce.isEmpty()) {
            StringBuilder msg = new StringBuilder("§c§l[DROP] §r");
            for (int i = 0; i < toForce.size(); i++) {
                msg.append(toForce.get(i).getItemMeta() != null
                    ? toForce.get(i).getItemMeta().getDisplayName()
                    : toForce.get(i).getType().name());
                if (i < toForce.size() - 1) msg.append("§7, ");
            }
            msg.append(" §7dropped on the ground!");

            if (player.getKiller() != null) {
                player.getKiller().sendMessage(msg.toString());
            }
            // Also tell nearby players
            player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(player.getLocation()) < 2500)
                .forEach(p -> p.sendMessage(msg.toString()));
        }
    }
}
