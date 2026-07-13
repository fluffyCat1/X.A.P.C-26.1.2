package com.xapc.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientShootCooldown {

    private static final Map<UUID, Integer> COOLDOWNS = new ConcurrentHashMap<>();

    private ClientShootCooldown() {}

    public static boolean isReady(UUID playerUuid) {
        return COOLDOWNS.getOrDefault(playerUuid, 0) <= 0;
    }

    public static void start(UUID playerUuid, int ticks) {
        COOLDOWNS.put(playerUuid, ticks);
    }

    public static void tick() {
        COOLDOWNS.replaceAll((uuid, ticks) -> Math.max(0, ticks - 1));
    }
}