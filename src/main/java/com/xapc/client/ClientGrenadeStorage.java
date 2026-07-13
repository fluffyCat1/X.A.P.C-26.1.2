package com.xapc.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientGrenadeStorage {

    private static final Map<UUID, Integer> GRENADES = new ConcurrentHashMap<>();

    private ClientGrenadeStorage() {}

    public static void set(UUID playerUuid, int count) {
        GRENADES.put(playerUuid, count);
    }

    public static int get(UUID playerUuid) {
        return GRENADES.getOrDefault(playerUuid, 0);
    }
}