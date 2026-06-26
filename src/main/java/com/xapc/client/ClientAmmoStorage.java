package com.xapc.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientAmmoStorage {
    private static final Map<UUID, Integer> ammoMap = new HashMap<>();

    public static int get(UUID uuid, int maxAmmo) {
        return ammoMap.getOrDefault(uuid, maxAmmo);
    }

    public static void set(UUID uuid, int ammo) {
        ammoMap.put(uuid, ammo);
    }
}