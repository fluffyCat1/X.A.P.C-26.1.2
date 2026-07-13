package com.xapc.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientGrenadeCooldown {
    private static final Map<UUID, Integer> cooldownTicks = new HashMap<>();

    public static void start(UUID uuid, int durationTicks) {
        cooldownTicks.put(uuid, durationTicks);
    }

    public static boolean isOnCooldown(UUID uuid) {
        return cooldownTicks.getOrDefault(uuid, 0) > 0;
    }

    public static void tick() {
        cooldownTicks.replaceAll((uuid, ticks) -> Math.max(0, ticks - 1));
        cooldownTicks.values().removeIf(ticks -> ticks <= 0);
    }
}