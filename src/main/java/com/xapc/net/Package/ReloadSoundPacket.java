package com.xapc.net.Package;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record ReloadSoundPacket(UUID playerUuid, Identifier soundId, boolean stop, float volume, float pitch)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ReloadSoundPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("xapc", "reload_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadSoundPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ReloadSoundPacket::playerUuid,
            Identifier.STREAM_CODEC, ReloadSoundPacket::soundId,
            ByteBufCodecs.BOOL, ReloadSoundPacket::stop,
            ByteBufCodecs.FLOAT, ReloadSoundPacket::volume,
            ByteBufCodecs.FLOAT, ReloadSoundPacket::pitch,
            ReloadSoundPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ReloadSoundPacket.TYPE, ReloadSoundPacket.CODEC);
    }
}