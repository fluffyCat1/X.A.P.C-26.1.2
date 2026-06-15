package com.xapc.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class NoHurtDelayMixin {

    private Vec3 lastVelocity;

    // 1. Перед началом получения урона запоминаем текущую скорость
    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void captureVelocity(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        this.lastVelocity = player.getDeltaMovement();
    }

    // 2. После получения урона возвращаем скорость обратно и сбрасываем статы боли
    @Inject(method = "hurtServer", at = @At("TAIL"))
    private void restoreVelocity(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        // Обнуляем таймеры замедления
        player.invulnerableTime = 0;
        player.hurtTime = 0;
        player.hurtDuration = 0;

        // Если скорость изменилась (уменьшилась) из-за удара — возвращаем старую
        if (this.lastVelocity != null) {
            player.setDeltaMovement(this.lastVelocity);
            player.hurtMarked = false; // Отменяем пометку "скорость изменилась" для сервера
        }
    }
}