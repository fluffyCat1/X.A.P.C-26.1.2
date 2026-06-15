package com.xapc.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class SpeedTrackerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void displaySpeed(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // Работаем только на стороне клиента

        // Получаем вектор движения
        Vec3 velocity = player.getDeltaMovement();

        // Считаем горизонтальную скорость
        double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);

        // Форматируем строку (округляем до 2 знаков)
        String speedText = String.format("%.2f", horizontalSpeed);

        // В новых версиях Mojang Mappings используем sendSystemMessage
        // Второй аргумент 'true' отвечает за то, что сообщение пойдет в Action Bar
        player.sendOverlayMessage(Component.literal("Скорость: " + speedText));
    }
}