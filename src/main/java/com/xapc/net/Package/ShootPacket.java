package com.xapc.net.Package;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

public record ShootPacket(InteractionHand hand) implements CustomPacketPayload {
    public static final Type<ShootPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("xapc", "shoot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShootPacket> CODEC =
            StreamCodec.composite(
                    StreamCodec.of((buf, h) -> buf.writeEnum(h), buf -> buf.readEnum(InteractionHand.class)),
                    ShootPacket::hand,
                    ShootPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
    }
}
