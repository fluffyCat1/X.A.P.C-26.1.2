package com.xapc.client.net;

import com.xapc.client.ClientAmmoStorage;
import com.xapc.net.Package.AmmoSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class AmmoSyncHandler {

    private AmmoSyncHandler() {}

    public static void handle(AmmoSyncPacket packet, ClientPlayNetworking.Context context) {
        ClientAmmoStorage.set(packet.playerUuid(), packet.ammo());
    }
}