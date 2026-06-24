package com.legendarysmp;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Creates legendary ItemStacks and registers their shaped crafting recipes.
 * Each weapon can only be crafted once per world (enforced by CraftingListener).
 */
public class WeaponManager {

    private final LegendarySMP plugin;

    /** PDC key used to store the weapon type ID string on the item */
    public final NamespacedKey WEAPON_KEY;

    /** PDC key used as a quick flag to check if any item is legendary */
    public final NamespacedKey LEGENDARY_KEY;

    // ─── Constructor ───────────────────────────────────────────────────────────

    public WeaponManager(LegendarySMP plugin) {
        this.plugin = plugin;
        this.WEAPON_KEY   = new NamespacedKey(plugin, "legendary_weapon");
        this.LEGENDARY_KEY = new NamespacedKey(plugin, "is_legendary");
    }

    // ─── Item Creation ─────────────────────────────────────────────────────────

    /**
     * Creates a fresh ItemStack for the given WeaponType with all metadata applied.
     * Can be called multiple times (e.g. on /legendary give).
     */
    public ItemStack createWeapon(WeaponType type) {
        ItemStack item = new ItemStack(type.material);
        ItemMeta meta  = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName(type.displayName);
        meta.setLore(type.lore);
        meta.setCustomModelData(type.modelData);
        meta.setUnbreakable(true);

        // 1.20.5+ clean glow — no enchant tooltip needed
        meta.setEnchantmentGlintOverride(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);

        // Tag as legendary
        meta.getPersistentDataContainer().set(WEAPON_KEY,   PersistentDataType.STRING, type.id);
        meta.getPersistentDataContainer().set(LEGENDARY_KEY, PersistentDataType.BYTE,  (byte) 1);

        item.setItemMeta(meta);

        // Apply weapon-specific attributes
        applyAttributes(item, type);

        return item;
    }

    /** Applies damage, attack speed, and reach attributes for each weapon. */
    private void applyAttributes(ItemStack item, WeaponType type) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        // Base attack damage (above the default 1.0)
        double damage = switch (type) {
            case SOULREAPER    -> 8.0;
            case TWIN_KINGS    -> 6.0;
            case FROSTFANG     -> 7.0;
            case TITANBREAKER  -> 9.0;
            case ECLIPSE_BLADE -> 7.0;
        };

        // 1.21.1: AttributeModifier uses NamespacedKey + EquipmentSlotGroup
        meta.addAttributeModifier(
            Attribute.GENERIC_ATTACK_DAMAGE,
            new AttributeModifier(
                new NamespacedKey(plugin, type.id + "_damage"),
                damage - 1,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            )
        );

        switch (type) {
            case SOULREAPER -> {
                // Slow attack speed (~1.0 swings/sec)
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                    new AttributeModifier(
                        new NamespacedKey(plugin, type.id + "_speed"),
                        -3.0, AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND));
            }
            case TITANBREAKER -> {
                // Very slow swing
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                    new AttributeModifier(
                        new NamespacedKey(plugin, type.id + "_speed"),
                        -2.8, AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND));
                // +1 reach — PLAYER_ENTITY_INTERACTION_RANGE is available in Paper 1.20.5+
                try {
                    Attribute reach = Attribute.valueOf("PLAYER_ENTITY_INTERACTION_RANGE");
                    meta.addAttributeModifier(reach,
                        new AttributeModifier(
                            new NamespacedKey(plugin, type.id + "_reach"),
                            1.0, AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.MAINHAND));
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().warning("[LegendarySMP] Reach attribute not found — skipping Titanbreaker +1 reach.");
                }
            }
            default -> {} // Other weapons use default attack speed
        }

        item.setItemMeta(meta);
    }

    // ─── Recipe Registration ───────────────────────────────────────────────────

    /**
     * Registers all 5 legendary crafting recipes.
     * Called once on plugin enable.
     */
    public void registerRecipes() {
        // Remove old recipes on reload to avoid duplicates
        for (WeaponType type : WeaponType.values()) {
            Bukkit.removeRecipe(new NamespacedKey(plugin, type.id));
        }

        registerSoulreaperRecipe();
        registerTwinKingsRecipe();
        registerFrostfangRecipe();
        registerTitanbreakerRecipe();
        registerEclipseBladeRecipe();

        plugin.getLogger().info("[LegendarySMP] Registered " + WeaponType.values().length + " legendary recipes.");
    }

    /**
     * Soulreaper — requires Netherite, Crying Obsidian, Ender Pearl.
     * <pre>
     *  N D N
     *  N D N
     *  N E N
     * </pre>
     */
    private void registerSoulreaperRecipe() {
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(plugin, "soulreaper"), createWeapon(WeaponType.SOULREAPER));
        r.shape("NDN", "NDN", "NEN");
        r.setIngredient('N', Material.NETHERITE_INGOT);
        r.setIngredient('D', Material.CRYING_OBSIDIAN);
        r.setIngredient('E', Material.ENDER_PEARL);
        Bukkit.addRecipe(r);
    }

    /**
     * Twin Kings — Gold swords forged with Blaze Rod and Netherite.
     * <pre>
     *  G D G
     *  G G G
     *  N B N
     * </pre>
     */
    private void registerTwinKingsRecipe() {
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(plugin, "twin_kings"), createWeapon(WeaponType.TWIN_KINGS));
        r.shape("GDG", "GGG", "NBN");
        r.setIngredient('G', Material.GOLD_INGOT);
        r.setIngredient('D', Material.DIAMOND);
        r.setIngredient('N', Material.NETHERITE_INGOT);
        r.setIngredient('B', Material.BLAZE_ROD);
        Bukkit.addRecipe(r);
    }

    /**
     * Frostfang — Ice layers surrounding a Diamond Sword.
     * <pre>
     *  I B I
     *  I D I
     *  I S I
     * </pre>
     */
    private void registerFrostfangRecipe() {
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(plugin, "frostfang"), createWeapon(WeaponType.FROSTFANG));
        r.shape("IBI", "IDI", "ISI");
        r.setIngredient('I', Material.ICE);
        r.setIngredient('B', Material.BLUE_ICE);
        r.setIngredient('D', Material.DIAMOND_SWORD);
        r.setIngredient('S', Material.PACKED_ICE);
        Bukkit.addRecipe(r);
    }

    /**
     * Titanbreaker — Anvils, Obsidian, and Netherite.
     * <pre>
     *  N A N
     *  N O N
     *  N A N
     * </pre>
     */
    private void registerTitanbreakerRecipe() {
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(plugin, "titanbreaker"), createWeapon(WeaponType.TITANBREAKER));
        r.shape("NAN", "NON", "NAN");
        r.setIngredient('N', Material.NETHERITE_INGOT);
        r.setIngredient('A', Material.ANVIL);
        r.setIngredient('O', Material.OBSIDIAN);
        Bukkit.addRecipe(r);
    }

    /**
     * Eclipse Blade — End Crystals, Purpur, and Netherite Sword.
     * <pre>
     *  E P E
     *  P D P
     *  E P E
     * </pre>
     */
    private void registerEclipseBladeRecipe() {
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(plugin, "eclipse_blade"), createWeapon(WeaponType.ECLIPSE_BLADE));
        r.shape("EPE", "PDP", "EPE");
        r.setIngredient('E', Material.END_CRYSTAL);
        r.setIngredient('P', Material.PURPUR_BLOCK);
        r.setIngredient('D', Material.NETHERITE_SWORD);
        Bukkit.addRecipe(r);
    }

    // ─── Identification Helpers ────────────────────────────────────────────────

    /**
     * Returns the WeaponType of the given ItemStack, or null if it's not a legendary weapon.
     */
    public WeaponType getWeaponType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(WEAPON_KEY, PersistentDataType.STRING);
        return WeaponType.fromId(id);
    }

    /**
     * Quick check: is this item any legendary weapon?
     */
    public boolean isLegendary(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(LEGENDARY_KEY, PersistentDataType.BYTE);
    }
}
