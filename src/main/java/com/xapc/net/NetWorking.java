package com.xapc.net;

import com.xapc.net.Package.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class NetWorking {

    private NetWorking() {}

    public static void register() {
        ShootPacket.register();
        PayloadTypeRegistry.clientboundPlay().register(AmmoSyncPacket.TYPE, AmmoSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AnimTriggerPacket.TYPE, AnimTriggerPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerAnimBroadcastPacket.TYPE, PlayerAnimBroadcastPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ShootPacket.TYPE, ShootPacketHandler::handle);
    }
}