package com.xapc.mixin.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {

    // Ссылаемся на приватное финальное поле name из оригинального класса KeyMapping
    @Shadow
    private String name;

    @Inject(method = "getTranslatedKeyMessage", at = @At("HEAD"), cancellable = true)
    private void onGetTranslatedKeyMessage(CallbackInfoReturnable<Component> cir) {
        // Проверяем идентификатор клавиши спринта по её названию в реестре игры
        if ("key.sprint".equals(this.name)) {

            // Возвращаем любой кастомный текст (например, красный "ОТКЛЮЧЕНО")
            cir.setReturnValue(Component.literal("§cИДИ НАХУЙ"));

            // Отменяем дальнейшее выполнение метода, чтобы игра не перезаписала значение
            cir.cancel();
        }
    }
}