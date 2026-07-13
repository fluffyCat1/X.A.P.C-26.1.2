package com.xapc.net;

import com.xapc.net.Package.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class NetWorking {

    private NetWorking() {}

    public static void register() {
        ShootPacket.register();
        PayloadTypeRegistry.clientboundPlay().register(AmmoSyncPacket.TYPE, AmmoSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GrenadeSyncPacket.TYPE, GrenadeSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AnimTriggerPacket.TYPE, AnimTriggerPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerAnimBroadcastPacket.TYPE, PlayerAnimBroadcastPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ReloadSoundPacket.TYPE, ReloadSoundPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MeleeAttackPacket.TYPE, MeleeAttackPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GrenadeStartCookPacket.TYPE, GrenadeStartCookPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(MeleeAttackPacket.TYPE, MeleeAttackPacketHandler::handle);
        ServerPlayNetworking.registerGlobalReceiver(ShootPacket.TYPE, ShootPacketHandler::handle);

        ServerPlayNetworking.registerGlobalReceiver(GrenadeStartCookPacket.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var stack = player.getItemInHand(payload.hand());
                if (stack.getItem() instanceof com.xapc.utils.GrenadesAbstractClass grenade) {
                    com.xapc.utils.GrenadesAbstractClass.beginCook(player, stack, payload.hand(), grenade);
                }
            });
        });
    }
}