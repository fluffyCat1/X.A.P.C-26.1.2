package com.xapc.client.net;

import com.xapc.client.ClientAnimationSetup;
import com.xapc.net.Package.PlayerAnimBroadcastPacket;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.Animation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class PlayerAnimBroadcastHandler {

    private PlayerAnimBroadcastHandler() {}

    public static void handle(PlayerAnimBroadcastPacket packet, ClientPlayNetworking.Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        mc.level.players().stream()
                .filter(p -> p.getUUID().equals(packet.playerUuid()))
                .findFirst()
                .ifPresent(player -> {
                    PlayerAnimationController controller = (PlayerAnimationController)
                            PlayerAnimationAccess.getPlayerAnimationLayer(player, ClientAnimationSetup.BASE_LAYER_ID);
                    if (controller == null) return;

                    Animation actionAnim = PlayerAnimResources.getAnimation(packet.animationId());
                    if (actionAnim != null) {
                        controller.triggerAnimation(ClientAnimationSetup.getChainedWithIdle(packet.animationId(), actionAnim));
                    } else {
                        controller.triggerAnimation(packet.animationId());
                    }
                });
    }
}