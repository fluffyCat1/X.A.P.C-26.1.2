package com.xapc.client.net;

import com.xapc.client.ClientGrenadeStorage;
import com.xapc.net.Package.GrenadeSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class GrenadeSyncHandler {

    private GrenadeSyncHandler() {}

    public static void handle(GrenadeSyncPacket packet, ClientPlayNetworking.Context context) {
        ClientGrenadeStorage.set(packet.playerUuid(), packet.count());
    }
}