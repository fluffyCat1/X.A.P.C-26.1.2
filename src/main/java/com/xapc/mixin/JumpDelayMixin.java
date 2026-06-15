package com.xapc.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class JumpDelayMixin {

    @Unique
    private int customJumpCooldown = 3;

    @Unique
    private static final int COOLDOWN_TICKS = 5;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void tickCooldown(CallbackInfo ci) {
        if (this.customJumpCooldown > 0) {
            this.customJumpCooldown--;
        }
    }

    // Внедряемся в метод прыжка и делаем его отменяемым
    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        // Проверяем, что это игрок
        if (!((Object) this instanceof Player player)) return;

        // Игнорируем креатив
        if (player.isCreative() || player.getAbilities().flying) return;

        // Если кулдаун еще идет — ОТМЕНЯЕМ прыжок
        if (this.customJumpCooldown > 0) {
            ci.cancel();
            return;
        }

        // Если кулдаун прошел — разрешаем прыжок и запускаем таймер заново
        this.customJumpCooldown = COOLDOWN_TICKS;
    }
}