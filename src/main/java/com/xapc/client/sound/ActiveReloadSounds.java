package com.xapc.client.sound;

import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ActiveReloadSounds {
    private static final Map<UUID, com.xapc.client.sound.EntityBoundSoundInstance> ACTIVE = new HashMap<>();

    private ActiveReloadSounds() {}

    public static void start(UUID playerUuid, com.xapc.client.sound.EntityBoundSoundInstance instance) {
        stop(playerUuid); // на случай если что-то уже играло
        ACTIVE.put(playerUuid, instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    public static void stop(UUID playerUuid) {
        com.xapc.client.sound.EntityBoundSoundInstance instance = ACTIVE.remove(playerUuid);
        if (instance != null) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
    }
}