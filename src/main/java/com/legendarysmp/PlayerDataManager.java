package com.legendarysmp;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple in-memory manager for per-player ability state.
 * Data is created on demand and cleared on server shutdown.
 */
public class PlayerDataManager {

    private final Map<UUID, PlayerData> dataMap = new HashMap<>();

    /**
     * Returns the PlayerData for a given player, creating it if it doesn't exist.
     */
    public PlayerData getData(Player player) {
        return dataMap.computeIfAbsent(player.getUniqueId(), PlayerData::new);
    }

    /**
     * Returns the PlayerData for a UUID directly (nullable if player has never joined).
     */
    public PlayerData getData(UUID uuid) {
        return dataMap.get(uuid);
    }

    /**
     * Removes a player's data (called on logout to free memory).
     */
    public void removeData(Player player) {
        dataMap.remove(player.getUniqueId());
    }

    /**
     * Called on plugin disable — clears all in-memory state.
     */
    public void saveAll() {
        // Extend this if you ever want to persist cooldown/stack data across restarts
        dataMap.clear();
    }
}
