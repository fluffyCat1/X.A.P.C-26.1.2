package com.xapc.client.animations;

import com.xapc.utils.WeaponsAbstractClass;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.AnimationController;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import static com.xapc.client.XapcClient.BASE_LAYER_ID;

public class GenericAnimations {

    public static void clientPlayerTick(Player player) {
        if (!(player instanceof AbstractClientPlayer clientPlayer)) {
            return;
        }

        // Берем предмет из руки
        ItemStack mainHandStack = clientPlayer.getItemBySlot(EquipmentSlot.MAINHAND);
        PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
                player, BASE_LAYER_ID);

            // Если игрок держит ЛЮБОЕ оружие нашего мода
            if (mainHandStack.getItem() instanceof WeaponsAbstractClass weapon) {

                // 1. Приоритет: перезарядка
                if (clientPlayer.isUsingItem() && clientPlayer.getUseItem() == mainHandStack) {
                    controller.triggerAnimation(weapon.getReloadAnimationId());
                    return;
                }

                net.minecraft.world.item.component.CustomData customData = mainHandStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                int shootTicks = customData != null ? customData.copyTag().getInt("ShootAnimTicks").orElse(0) : 0;

                // 2. Приоритет: выстрел (работает, пока тикает таймер)
                if (shootTicks > 0) {
                    controller.triggerAnimation(weapon.getShootAnimationId());
                    return;
                }

                // 3. Приоритет: обычное удержание (idle)
                if (!controller.isActive()) {
                    controller.triggerAnimation(weapon.getIdleAnimationId());
            }
        }
    }
}