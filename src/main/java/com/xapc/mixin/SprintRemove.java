package com.xapc.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class SprintRemove {

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void onSetSprinting(boolean sprinting, CallbackInfo ci) {
        // Проверяем, является ли сущность игроком
        if ((Object) this instanceof Player) {
            // Если кто-то (код игры или пакет от клиента) пытается включить спринт (true)
            if (sprinting) {
                // Принудительно вызываем метод с флагом false, чтобы сбросить состояние
                ((LivingEntity) (Object) this).setSprinting(false);
                // Отменяем выполнение оригинального метода, чтобы он не перезаписал наш false
                ci.cancel();
            }
        }
    }
}