package com.xapc.net.Package;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import java.util.UUID;

public record AmmoSyncPacket(UUID playerUuid, int ammo) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("xapc", "ammo_sync");
    public static final Type<AmmoSyncPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, AmmoSyncPacket> CODEC = StreamCodec.of(
            (buf, pkt) -> { buf.writeUUID(pkt.playerUuid); buf.writeInt(pkt.ammo); },
            buf -> new AmmoSyncPacket(buf.readUUID(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}