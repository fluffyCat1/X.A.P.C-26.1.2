package com.xapc.net.Package;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record AnimTriggerPacket(UUID playerUuid, long instanceId, String controllerName, String animName, boolean stop) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("xapc", "anim_trigger");
    public static final Type<AnimTriggerPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, AnimTriggerPacket> CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUUID(pkt.playerUuid);
                buf.writeLong(pkt.instanceId);
                buf.writeUtf(pkt.controllerName);
                buf.writeUtf(pkt.animName);
                buf.writeBoolean(pkt.stop);
            },
            buf -> new AnimTriggerPacket(buf.readUUID(), buf.readLong(), buf.readUtf(), buf.readUtf(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}