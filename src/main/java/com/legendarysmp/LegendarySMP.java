package com.legendarysmp;

import com.legendarysmp.commands.LegendaryCommand;
import com.legendarysmp.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * LegendarySMP — Paper plugin (1.20.1+)
 *
 * 5 unique legendary weapons, each craftable exactly once per world.
 * Dragon Egg gives Speed I and always drops on death.
 * All legendaries bypass keepInventory on death.
 *
 * ─── Commands ────────────────────────────────────────────
 * /legendary give   <player> <id>  — Give a weapon
 * /legendary egg    <player>       — Give the Dragon Egg
 * /legendary status                — Show crafted status
 * /legendary reset  <id|all>       — Allow re-craft
 * /legendary reload                — Reload config
 * ─────────────────────────────────────────────────────────
 */
public class LegendarySMP extends JavaPlugin {

    private static LegendarySMP instance;
    private WeaponManager      weaponManager;
    private DragonEggManager   dragonEggManager;
    private PlayerDataManager  playerDataManager;

    // ─── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        instance = this;

        // Config
        saveDefaultConfig();

        // Managers
        playerDataManager = new PlayerDataManager();
        weaponManager     = new WeaponManager(this);
        weaponManager.registerRecipes();
        dragonEggManager  = new DragonEggManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new WeaponAbilityListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);

        // Dragon egg speed task
        dragonEggManager.startEffectTask();

        // Commands
        LegendaryCommand cmd = new LegendaryCommand(this);
        getCommand("legendary").setExecutor(cmd);
        getCommand("legendary").setTabCompleter(cmd);

        getLogger().info("══════════════════════════════════════════");
        getLogger().info("  LegendarySMP enabled!");
        getLogger().info("  " + WeaponType.values().length + " legendary weapons registered.");
        getLogger().info("══════════════════════════════════════════");
    }

    @Override
    public void onDisable() {
        playerDataManager.saveAll();
        getLogger().info("[LegendarySMP] Plugin disabled. Goodbye!");
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public static LegendarySMP getInstance()            { return instance; }
    public WeaponManager      getWeaponManager()        { return weaponManager; }
    public DragonEggManager   getDragonEggManager()     { return dragonEggManager; }
    public PlayerDataManager  getPlayerDataManager()    { return playerDataManager; }
}
