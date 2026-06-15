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
        // Получаем слой анимации PAL игрока
        var layer = PlayerAnimationAccess.getPlayerAnimationLayer(clientPlayer, BASE_LAYER_ID);


            // Если игрок держит ЛЮБОЕ оружие нашего мода
            if (mainHandStack.getItem() instanceof WeaponsAbstractClass weapon) {

                // 1. Приоритет: перезарядка
                if (clientPlayer.isUsingItem() && clientPlayer.getUseItem() == mainHandStack) {
                    controller.triggerAnimation(weapon.getReloadAnimationId());
                    return;
                }

                // 2. Приоритет: выстрел (отслеживаем замах руки от левого клика)
                if (clientPlayer.swinging && clientPlayer.swingTime == 0) {
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