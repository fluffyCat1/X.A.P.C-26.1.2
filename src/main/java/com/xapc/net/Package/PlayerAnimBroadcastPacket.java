package com.xapc.net.Package;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record PlayerAnimBroadcastPacket(UUID playerUuid, Identifier animationId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("xapc", "player_anim_broadcast");
    public static final Type<PlayerAnimBroadcastPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PlayerAnimBroadcastPacket> CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUUID(pkt.playerUuid());
                buf.writeUtf(pkt.animationId().toString());
            },
            buf -> new PlayerAnimBroadcastPacket(
                    buf.readUUID(),
                    Identifier.parse(buf.readUtf())
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}