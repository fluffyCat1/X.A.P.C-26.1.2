package com.xapc.net.Package;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AnimTriggerPacket(long instanceId, String controllerName, String animName, boolean stop) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("xapc", "anim_trigger");
    public static final Type<AnimTriggerPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, AnimTriggerPacket> CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeLong(pkt.instanceId);
                buf.writeUtf(pkt.controllerName);
                buf.writeUtf(pkt.animName);
                buf.writeBoolean(pkt.stop);
            },
            buf -> new AnimTriggerPacket(buf.readLong(), buf.readUtf(), buf.readUtf(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}