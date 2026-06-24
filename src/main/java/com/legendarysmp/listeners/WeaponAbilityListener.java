package com.legendarysmp.listeners;

import com.legendarysmp.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Handles all 5 legendary weapon abilities.
 *
 * ─── SOULREAPER  ── Kill → Regen II 5s │ Crit → +10% dmg
 * ─── TWIN KINGS  ── Hit → Rage stack   │ 5 stacks → +50% next hit
 * ─── FROSTFANG   ── Every 3rd Crit → Freeze 3s
 * ─── TITANBREAKER── Hit blocking → Break shield │ Charged → Knockback
 * ─── ECLIPSE BLADE─ RClick → Dash 6b  │ Next hit → +30% dmg (20s CD)
 */
public class WeaponAbilityListener implements Listener {

    private final LegendarySMP plugin;
    private final WeaponManager wm;
    private final PlayerDataManager pdm;

    public WeaponAbilityListener(LegendarySMP plugin) {
        this.plugin = plugin;
        this.wm     = plugin.getWeaponManager();
        this.pdm    = plugin.getPlayerDataManager();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Combat Event — EntityDamageByEntity
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target))  return;

        ItemStack held = attacker.getInventory().getItemInMainHand();
        WeaponType type = wm.getWeaponType(held);
        if (type == null) return;

        boolean isCrit = isCriticalHit(attacker);
        PlayerData data = pdm.getData(attacker);

        switch (type) {
            case SOULREAPER    -> handleSoulreaperHit(event, attacker, isCrit);
            case TWIN_KINGS    -> handleTwinKingsHit(event, attacker, target, data);
            case FROSTFANG     -> handleFrostfangHit(event, attacker, target, data, isCrit);
            case TITANBREAKER  -> handleTitanbreakerHit(event, attacker, target);
            case ECLIPSE_BLADE -> handleEclipseBladeHit(event, attacker, data);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Kill Event — EntityDeath (Soulreaper kill proc)
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack held = killer.getInventory().getItemInMainHand();
        if (wm.getWeaponType(held) != WeaponType.SOULREAPER) return;

        // Regen II for 5 seconds (100 ticks)
        int regenTicks = plugin.getConfig().getInt("weapons.soulreaper.regen-duration", 100);
        killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenTicks, 1, false, true, true));
        killer.sendActionBar("§5✦ Soul Harvested — §fRegeneration II ✦");

        // Visual feedback
        killer.getWorld().spawnParticle(Particle.SOUL, killer.getLocation().add(0, 1, 0),
            20, 0.4, 0.6, 0.4, 0.02);
        killer.getWorld().playSound(killer.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 2.0f);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Right-Click — Eclipse Blade dash
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (wm.getWeaponType(held) != WeaponType.ECLIPSE_BLADE) return;

        event.setCancelled(true); // Prevent block interaction
        triggerEclipseDash(player, pdm.getData(player));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Individual Weapon Handlers
    // ══════════════════════════════════════════════════════════════════════════

    // ── 1. SOULREAPER ─────────────────────────────────────────────────────────

    private void handleSoulreaperHit(EntityDamageByEntityEvent event,
                                     Player attacker, boolean isCrit) {
        if (isCrit) {
            double bonus = plugin.getConfig().getDouble("weapons.soulreaper.crit-bonus", 0.10);
            event.setDamage(event.getDamage() * (1.0 + bonus));
            attacker.sendActionBar("§5✦ Soul Crit! §f+" + (int)(bonus * 100) + "% dmg");
        }
        // Kill effect handled in onEntityDeath above
    }

    // ── 2. TWIN KINGS ─────────────────────────────────────────────────────────

    private void handleTwinKingsHit(EntityDamageByEntityEvent event,
                                    Player attacker, LivingEntity target, PlayerData data) {
        long expireMs = plugin.getConfig().getLong("weapons.twin-kings.rage-expire-ms", 15000);
        data.tickRageDecay(expireMs);

        if (data.rageReady) {
            // ── Consume rage for +50% damage ──────────────────────────────────
            double bonus = plugin.getConfig().getDouble("weapons.twin-kings.rage-damage-bonus", 0.50);
            event.setDamage(event.getDamage() * (1.0 + bonus));
            data.rageStacks = 0;
            data.rageReady  = false;

            attacker.sendActionBar("§c§l⚔ RAGE UNLEASHED! §r§c+" + (int)(bonus * 100) + "% ⚔");
            attacker.getWorld().spawnParticle(Particle.FLAME,
                target.getLocation().add(0, 1, 0), 30, 0.4, 0.5, 0.4, 0.1);
            attacker.getWorld().playSound(attacker.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_GROWL, 0.4f, 2.0f);

        } else {
            // ── Add rage stack ────────────────────────────────────────────────
            int max = plugin.getConfig().getInt("weapons.twin-kings.rage-stacks-max", 5);
            data.rageStacks = Math.min(data.rageStacks + 1, max);
            data.lastRageTime = System.currentTimeMillis();

            if (data.rageStacks >= max) {
                data.rageReady = true;
                attacker.sendActionBar("§c§l⚔ FULL RAGE! Next hit +" +
                    (int)(plugin.getConfig().getDouble("weapons.twin-kings.rage-damage-bonus", 0.50) * 100) + "% ⚔");
                attacker.getWorld().playSound(attacker.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.4f, 1.5f);
            } else {
                attacker.sendActionBar("§6⚔ Rage: " + data.rageStacks + "/" + max);
            }
        }
    }

    // ── 3. FROSTFANG ──────────────────────────────────────────────────────────

    private void handleFrostfangHit(EntityDamageByEntityEvent event,
                                    Player attacker, LivingEntity target,
                                    PlayerData data, boolean isCrit) {
        if (!isCrit) return; // Only count critical hits

        int needed = plugin.getConfig().getInt("weapons.frostfang.freeze-crits-needed", 3);
        data.frostCrits++;

        if (data.frostCrits >= needed) {
            data.frostCrits = 0;

            // ── Freeze the target ─────────────────────────────────────────────
            int freezeTicks = plugin.getConfig().getInt("weapons.frostfang.freeze-duration", 60);

            // Full powder-snow freeze effect
            target.setFreezeTicks(target.getMaxFreezeTicks());
            // Heavy slowness to simulate immobility
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,    freezeTicks, 10, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,  freezeTicks, -5, false, false));

            // Unfreeze after duration
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (target.isValid()) target.setFreezeTicks(0);
                }
            }.runTaskLater(plugin, freezeTicks);

            // Feedback
            attacker.sendActionBar("§b❄ FROZEN! ❄");
            if (target instanceof Player p) p.sendActionBar("§b❄ YOU ARE FROZEN! ❄");

            target.getWorld().spawnParticle(Particle.SNOWFLAKE,
                target.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.05);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, 1.5f, 0.5f);

        } else {
            attacker.sendActionBar("§b❄ Frost Crits: " + data.frostCrits + "/" + needed);
        }
    }

    // ── 4. TITANBREAKER ───────────────────────────────────────────────────────

    private void handleTitanbreakerHit(EntityDamageByEntityEvent event,
                                       Player attacker, LivingEntity target) {
        // ── Shield Break ──────────────────────────────────────────────────────
        if (target instanceof Player targetPlayer && targetPlayer.isBlocking()) {
            int shieldCD = plugin.getConfig().getInt("weapons.titanbreaker.shield-break-cooldown", 100);
            targetPlayer.setCooldown(org.bukkit.Material.SHIELD, shieldCD);

            targetPlayer.sendActionBar("§8§l🛡 SHIELD SHATTERED!");
            attacker.sendActionBar("§8§l🛡 Shield Broken!");
            targetPlayer.getWorld().playSound(targetPlayer.getLocation(),
                Sound.ITEM_SHIELD_BREAK, 1.2f, 0.8f);
            targetPlayer.getWorld().spawnParticle(Particle.CRIT,
                targetPlayer.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.1);
        }

        // ── Charged Hit Knockback ─────────────────────────────────────────────
        float threshold = (float) plugin.getConfig().getDouble("weapons.titanbreaker.charged-threshold", 0.9);
        if (attacker.getAttackCooldown() >= threshold) {
            double mult = plugin.getConfig().getDouble("weapons.titanbreaker.knockback-multiplier", 3.0);
            Vector dir = target.getLocation().subtract(attacker.getLocation()).toVector().normalize();
            dir.setY(0.45);
            target.setVelocity(dir.multiply(mult));

            attacker.sendActionBar("§8§l💥 TITANSLAM!");
            attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.7f);
        }
    }

    // ── 5. ECLIPSE BLADE ──────────────────────────────────────────────────────

    private void handleEclipseBladeHit(EntityDamageByEntityEvent event,
                                       Player attacker, PlayerData data) {
        if (!data.dashBonusActive) return;

        double bonus = plugin.getConfig().getDouble("weapons.eclipse-blade.dash-bonus", 0.30);
        event.setDamage(event.getDamage() * (1.0 + bonus));
        data.dashBonusActive = false;

        attacker.sendActionBar("§d§l✦ VOID STRIKE! +" + (int)(bonus * 100) + "% ✦");
        attacker.getWorld().spawnParticle(Particle.PORTAL,
            attacker.getLocation().add(0, 1, 0), 25, 0.3, 0.5, 0.3, 0.15);
    }

    private void triggerEclipseDash(Player player, PlayerData data) {
        long cdMs = plugin.getConfig().getLong("weapons.eclipse-blade.dash-cooldown-ms", 20000);

        if (!data.isDashReady(cdMs)) {
            player.sendActionBar("§d⚡ Dash cooldown: §f" + data.dashCooldownRemaining(cdMs) + "s");
            return;
        }

        // ── Perform dash ───────────────────────────────────────────────────────
        data.eclipseDashTimestamp = System.currentTimeMillis();
        data.dashBonusActive      = true;

        double dist = plugin.getConfig().getDouble("weapons.eclipse-blade.dash-distance", 6.0);
        Vector dir  = player.getLocation().getDirection().normalize().multiply(dist / 5.0);
        dir.setY(Math.max(dir.getY(), 0.1));
        player.setVelocity(dir);

        // Visual + audio
        player.getWorld().spawnParticle(Particle.PORTAL,
            player.getLocation().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.4f);
        player.sendActionBar("§d§l⚡ ECLIPSE DASH! ⚡");

        // Cancel dash bonus if the player doesn't hit within 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                data.dashBonusActive = false;
            }
        }.runTaskLater(plugin, 100L);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Utility
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Determines whether the player's last attack was a critical hit.
     * A crit in vanilla Minecraft requires:
     *   • Player is falling (not on ground)
     *   • Not sprinting
     *   • Not inside a vehicle
     *   • Not affected by Blindness
     *   • Attack cooldown ≥ 90%
     */
    private boolean isCriticalHit(Player player) {
        return player.getFallDistance() > 0.0F
            && !player.isOnGround()
            && !player.isInsideVehicle()
            && !player.hasPotionEffect(PotionEffectType.BLINDNESS)
            && !player.isSprinting()
            && player.getAttackCooldown() >= 0.9f;
    }
}
