package com.xapc.client.animations;

import com.xapc.client.ClientAnimationSetup;
import com.xapc.utils.WeaponsAbstractClass;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GenericAnimations {

    public static void clientPlayerTick(Player player) {
        if (!(player instanceof AbstractClientPlayer clientPlayer)) return;

        PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess
                .getPlayerAnimationLayer(clientPlayer, ClientAnimationSetup.BASE_LAYER_ID);
        if (controller == null) return;

        ItemStack stack = clientPlayer.getItemBySlot(EquipmentSlot.MAINHAND);

        if (!(stack.getItem() instanceof WeaponsAbstractClass weapon)) {
            controller.stop();
            return;
        }

        if (!controller.isActive()) {
            controller.triggerAnimation(weapon.getIdleAnimationId());
        }
    }
}