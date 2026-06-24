package com.legendarysmp;

import org.bukkit.Material;
import java.util.List;

/**
 * Defines all 5 legendary weapons.
 * Each weapon has a unique ID, custom model data integer (for resource pack),
 * base material, display name, and lore.
 */
public enum WeaponType {

    SOULREAPER(
        "soulreaper",
        1001,
        Material.IRON_HOE,           // Resource pack overrides to scythe model
        "§5§lSoulreaper",
        List.of(
            "§7A dark scythe that thirsts for souls.",
            "",
            "§dKill: §fRegeneration II (5s)",
            "§dCrit: §f+10% bonus damage",
            "§8⚠ Slow attack speed",
            "",
            "§c§l✦ LEGENDARY ✦"
        )
    ),

    TWIN_KINGS(
        "twin_kings",
        1002,
        Material.IRON_SWORD,         // Resource pack overrides to dual-sword model
        "§6§lTwin Kings",
        List.of(
            "§7Two royal blades forged as one.",
            "",
            "§6Hit: §f+1 Rage Stack (max 5)",
            "§65 Stacks: §fNext hit +50% damage",
            "§7Stacks expire after §e15 seconds",
            "",
            "§c§l✦ LEGENDARY ✦"
        )
    ),

    FROSTFANG(
        "frostfang",
        1003,
        Material.IRON_SWORD,         // Resource pack overrides to ice sword model
        "§b§lFrostfang",
        List.of(
            "§7A blade carved from eternal ice.",
            "",
            "§bEvery 3rd Crit: §fFreeze target (3s)",
            "",
            "§c§l✦ LEGENDARY ✦"
        )
    ),

    TITANBREAKER(
        "titanbreaker",
        1004,
        Material.IRON_HOE,           // Resource pack overrides to mace-sword hybrid
        "§8§lTitanbreaker",
        List.of(
            "§7A weapon of devastating force.",
            "",
            "§8Instantly breaks shields",
            "§8Charged hit: §fMassive knockback",
            "§8+1 block reach",
            "§8⚠ Slow swing speed",
            "",
            "§c§l✦ LEGENDARY ✦"
        )
    ),

    ECLIPSE_BLADE(
        "eclipse_blade",
        1005,
        Material.IRON_SWORD,         // Resource pack overrides to dark energy blade
        "§d§lEclipse Blade",
        List.of(
            "§7A blade from the void between worlds.",
            "",
            "§dRight-click: §fDash 6 blocks",
            "§dPost-dash hit: §f+30% damage",
            "§7Cooldown: §d20 seconds",
            "",
            "§c§l✦ LEGENDARY ✦"
        )
    );

    // ─── Fields ────────────────────────────────────────────────────────────────

    /** Unique string identifier, used in config and PDC */
    public final String id;

    /** Custom model data integer — map this in your resource pack */
    public final int modelData;

    /** Base Bukkit material (overridden visually by resource pack) */
    public final Material material;

    /** Coloured display name */
    public final String displayName;

    /** Item lore lines */
    public final List<String> lore;

    // ─── Constructor ───────────────────────────────────────────────────────────

    WeaponType(String id, int modelData, Material material, String displayName, List<String> lore) {
        this.id = id;
        this.modelData = modelData;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    /** Returns the WeaponType matching the given ID string, or null if not found. */
    public static WeaponType fromId(String id) {
        if (id == null) return null;
        for (WeaponType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }

    /** Returns a comma-separated list of all weapon IDs (for help messages). */
    public static String allIds() {
        StringBuilder sb = new StringBuilder();
        WeaponType[] values = values();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i].id);
            if (i < values.length - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
