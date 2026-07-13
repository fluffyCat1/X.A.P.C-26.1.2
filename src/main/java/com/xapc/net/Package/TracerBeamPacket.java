package com.xapc.net.Package;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TracerBeamPacket(double startX, double startY, double startZ,
                               double endX, double endY, double endZ) implements CustomPacketPayload {
    public static final Type<TracerBeamPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("xapc", "tracer_beam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TracerBeamPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, TracerBeamPacket::startX,
                    ByteBufCodecs.DOUBLE, TracerBeamPacket::startY,
                    ByteBufCodecs.DOUBLE, TracerBeamPacket::startZ,
                    ByteBufCodecs.DOUBLE, TracerBeamPacket::endX,
                    ByteBufCodecs.DOUBLE, TracerBeamPacket::endY,
                    ByteBufCodecs.DOUBLE, TracerBeamPacket::endZ,
                    TracerBeamPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }
}