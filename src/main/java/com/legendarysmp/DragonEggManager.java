package com.legendarysmp;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Handles the Dragon Egg — the server's main PvP objective.
 *
 * Active effects while holding the egg:
 *   - Speed I (persistent, reapplied every second)
 *
 * Death behaviour (handled by DeathListener):
 *   - The egg always drops on death, bypassing keepInventory.
 *
 * Owner reveal: DISABLED per server configuration.
 */
public class DragonEggManager {

    private final LegendarySMP plugin;

    /** PDC key that marks an item as the legendary Dragon Egg */
    public final NamespacedKey EGG_KEY;

    // ─── Constructor ───────────────────────────────────────────────────────────

    public DragonEggManager(LegendarySMP plugin) {
        this.plugin  = plugin;
        this.EGG_KEY = new NamespacedKey(plugin, "is_dragon_egg");
    }

    // ─── Item Creation ─────────────────────────────────────────────────────────

    /**
     * Creates the legendary Dragon Egg ItemStack with PDC tag and custom lore.
     */
    public ItemStack createDragonEgg() {
        ItemStack egg  = new ItemStack(Material.DRAGON_EGG);
        ItemMeta  meta = egg.getItemMeta();
        assert meta != null;

        meta.setDisplayName("§5§l✦ Dragon Egg ✦");
        meta.setLore(List.of(
            "§7The ancient source of draconic power.",
            "",
            "§dWhile held: §fSpeed I",
            "",
            "§c§lALWAYS DROPS ON DEATH",
            "§8Guard it with your life."
        ));
        meta.setUnbreakable(true);
        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE,
            org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES
        );

        meta.getPersistentDataContainer().set(EGG_KEY, PersistentDataType.BYTE, (byte) 1);
        egg.setItemMeta(meta);
        return egg;
    }

    // ─── Effect Task ───────────────────────────────────────────────────────────

    /**
     * Starts a repeating task that gives Speed I to any online player
     * currently holding the Dragon Egg in their inventory.
     * Runs every 20 ticks (1 second). Effect duration is 40 ticks so it
     * stays seamlessly active.
     */
    public void startEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isHoldingEgg(player)) {
                        applyEggEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // ─── Identification ────────────────────────────────────────────────────────

    /**
     * Returns true if the given ItemStack is the legendary Dragon Egg.
     */
    public boolean isDragonEgg(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(EGG_KEY, PersistentDataType.BYTE);
    }

    /**
     * Returns true if the player has the Dragon Egg anywhere in their inventory.
     */
    public boolean isHoldingEgg(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isDragonEgg(item)) return true;
        }
        return false;
    }

    // ─── Private Helpers ───────────────────────────────────────────────────────

    /** Applies Speed I. Use ambient=true and particles=false for a clean look. */
    private void applyEggEffects(Player player) {
        if (plugin.getConfig().getBoolean("dragon-egg.speed-buff", true)) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                40,    // duration: 2 seconds (refreshed every second → seamless)
                0,     // amplifier: Speed I
                true,  // ambient: softer particles
                false, // particles: hide (no spam)
                true   // icon: show in HUD
            ));
        }
    }
}
