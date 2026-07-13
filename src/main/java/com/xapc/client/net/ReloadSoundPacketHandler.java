package com.xapc.client.net;

import com.xapc.client.sound.ActiveReloadSounds;
import com.xapc.client.sound.EntityBoundSoundInstance;
import com.xapc.net.Package.ReloadSoundPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Optional;

public final class ReloadSoundPacketHandler {

    private ReloadSoundPacketHandler() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ReloadSoundPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {

                var level = context.client().level;
                if (level == null) return;

                var entity = level.getPlayerByUUID(payload.playerUuid());
                if (entity == null) return;

                Optional<Holder.Reference<SoundEvent>> soundHolder = BuiltInRegistries.SOUND_EVENT.get(payload.soundId());
                if (soundHolder.isEmpty()) return;

                SoundEvent sound = soundHolder.get().value(); // <-- достаём реальный SoundEvent

                ActiveReloadSounds.start(payload.playerUuid(),
                        new EntityBoundSoundInstance(sound, SoundSource.PLAYERS, entity, payload.volume(), payload.pitch()));
            });
        });
    }
}