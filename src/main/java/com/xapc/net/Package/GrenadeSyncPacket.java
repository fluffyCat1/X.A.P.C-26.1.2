package com.xapc.net.Package;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import java.util.UUID;

public record GrenadeSyncPacket(UUID playerUuid, int count) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("xapc", "grenade_sync");
    public static final Type<GrenadeSyncPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, GrenadeSyncPacket> CODEC = StreamCodec.of(
            (buf, pkt) -> { buf.writeUUID(pkt.playerUuid); buf.writeInt(pkt.count); },
            buf -> new GrenadeSyncPacket(buf.readUUID(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}