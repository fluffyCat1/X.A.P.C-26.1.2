package com.xapc.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class JumpRemoveInertia {
    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void onJumpFromGround(CallbackInfo ci) {
        if ((Object) this instanceof Player player) {

            Vec3 velocity = player.getDeltaMovement();

            // 1. Получаем текущее значение атрибута скорости игрока.
            // У обычного игрока без баффов это значение равно 0.1
            double attributeSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);

            // 2. Считаем динамический лимит.
            // Базовый лимит 0.28 при значении атрибута 0.1 означает коэффициент соотношения 2.8.
            // Умножаем текущее значение атрибута на 2.8, чтобы пропорционально масштабировать лимит.
            double dynamicLimit = attributeSpeed * 1.2;

            double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);

            // 3. Если скорость превышает динамический лимит, гасим накопленный b-hop
            if (horizontalSpeed > dynamicLimit) {
                double reduction = dynamicLimit / horizontalSpeed;
                player.setDeltaMovement(velocity.x * reduction, velocity.y, velocity.z * reduction);
            }
        }
    }
}