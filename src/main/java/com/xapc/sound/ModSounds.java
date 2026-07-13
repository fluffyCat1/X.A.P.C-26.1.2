package com.xapc.sound;

import com.xapc.Xapc;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {

    public static final SoundEvent SHOOTGUN4_SHOOT = register("shootgun4_shoot");
    public static final SoundEvent SHOOTGUN4_RELOAD = register("shootgun4_reload");
    public static final SoundEvent BUTTSTROKE = register("buttstroke");
    public static final SoundEvent HITSOUND = register("hitsound");
    public static final SoundEvent HIT_MELEE = register("hit-melee");
    private ModSounds() {}

    private static SoundEvent register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(Xapc.MOD_ID, path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
        Xapc.LOGGER.info("Registered sounds: {}, {}", SHOOTGUN4_SHOOT.location(), SHOOTGUN4_RELOAD.location());
    }
}