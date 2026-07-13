package com.xapc.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientEquipLock {
    private static final Map<UUID, Integer> lockTicks = new HashMap<>();

    public static void start(UUID uuid, int durationTicks) {
        lockTicks.put(uuid, durationTicks);
    }

    public static boolean isLocked(UUID uuid) {
        return lockTicks.getOrDefault(uuid, 0) > 0;
    }

    public static void tick() {
        lockTicks.replaceAll((uuid, ticks) -> Math.max(0, ticks - 1));
        lockTicks.values().removeIf(ticks -> ticks <= 0);
    }
}