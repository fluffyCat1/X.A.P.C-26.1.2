package com.xapc.net.Package;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

public record MeleeAttackPacket(InteractionHand hand) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MeleeAttackPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("xapc", "melee_attack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MeleeAttackPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(
                            id -> InteractionHand.values()[id],   // int -> InteractionHand
                            InteractionHand::ordinal               // InteractionHand -> int
                    ),
                    MeleeAttackPacket::hand,
                    MeleeAttackPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}