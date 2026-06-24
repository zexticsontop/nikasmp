package com.legendarysmp;

import java.util.UUID;

/**
 * Stores per-player state for legendary weapon abilities.
 * All fields are in-memory only; reset when server restarts.
 */
public class PlayerData {

    public final UUID uuid;

    // ── Twin Kings: Rage System ─────────────────────────────────────────────────
    /** Current rage stacks (0–5) */
    public int rageStacks = 0;
    /** Timestamp of the last rage stack gain (ms) */
    public long lastRageTime = 0;
    /** True when stacks hit 5 and the next hit should deal +50% */
    public boolean rageReady = false;

    // ── Frostfang: Crit Counter ─────────────────────────────────────────────────
    /** Number of critical hits landed with Frostfang this cycle (resets at 3) */
    public int frostCrits = 0;

    // ── Eclipse Blade: Dash System ──────────────────────────────────────────────
    /** Timestamp when the last dash was used (ms) */
    public long eclipseDashTimestamp = 0;
    /** True when a dash was just performed; next hit will deal +30% */
    public boolean dashBonusActive = false;

    // ── Soulreaper ──────────────────────────────────────────────────────────────
    // (no extra state needed; handled reactively on kill event)

    // ── Constructor ─────────────────────────────────────────────────────────────

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Check and expire rage stacks if they've been idle for more than the
     * configured duration. Call before reading rageStacks.
     *
     * @param expireMs rage expire time in milliseconds (from config, default 15000)
     */
    public void tickRageDecay(long expireMs) {
        if (rageStacks > 0 && System.currentTimeMillis() - lastRageTime > expireMs) {
            rageStacks = 0;
            rageReady = false;
        }
    }

    /** Returns true if the Eclipse Blade dash is currently off cooldown. */
    public boolean isDashReady(long cooldownMs) {
        return System.currentTimeMillis() - eclipseDashTimestamp >= cooldownMs;
    }

    /** Returns the remaining dash cooldown in seconds (0 if ready). */
    public long dashCooldownRemaining(long cooldownMs) {
        long remaining = cooldownMs - (System.currentTimeMillis() - eclipseDashTimestamp);
        return Math.max(0, remaining / 1000);
    }
}
