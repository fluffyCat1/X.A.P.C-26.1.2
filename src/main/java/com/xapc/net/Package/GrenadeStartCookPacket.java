package com.xapc.net.Package;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

public record GrenadeStartCookPacket(InteractionHand hand) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("xapc", "grenade_start_cook");
    public static final Type<GrenadeStartCookPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, GrenadeStartCookPacket> CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeEnum(pkt.hand),
            buf -> new GrenadeStartCookPacket(buf.readEnum(InteractionHand.class))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}