package com.xapc.client.net;

import com.xapc.net.Package.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientNetworkingInit {

    private ClientNetworkingInit() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(AmmoSyncPacket.TYPE, AmmoSyncHandler::handle);
        ClientPlayNetworking.registerGlobalReceiver(AnimTriggerPacket.TYPE, AnimTriggerHandler::handle);
        ClientPlayNetworking.registerGlobalReceiver(PlayerAnimBroadcastPacket.TYPE, PlayerAnimBroadcastHandler::handle);
        ClientPlayNetworking.registerGlobalReceiver(GrenadeSyncPacket.TYPE, GrenadeSyncHandler::handle);
        TracerBeamPacketHandler.register();
    }
}