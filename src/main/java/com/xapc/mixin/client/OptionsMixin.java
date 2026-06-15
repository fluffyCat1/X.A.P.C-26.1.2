package com.xapc.mixin.client;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class OptionsMixin {

    @Shadow
    private OptionInstance<Boolean> toggleSprint;

    // Сюда мы сохраним нашу кастомную обёртку кнопки, чтобы не пересоздавать её каждый кадр
    private OptionInstance<Boolean> customToggleSprintWrapper;

    @Inject(method = "toggleSprint", at = @At("HEAD"), cancellable = true)
    private void onGetToggleSprint(CallbackInfoReturnable<OptionInstance<Boolean>> cir) {
        // Если оригинальная опция ещё не создалась игрой, ничего не делаем
        if (this.toggleSprint == null) {
            return;
        }

        // Создаем нашу кастомную кнопку один раз
        if (this.customToggleSprintWrapper == null) {
            this.customToggleSprintWrapper = new OptionInstance<>(
                    "", // Ключ названия опции ("Режим бега")
                    OptionInstance.noTooltip(), // Тултип (подсказка при наведении), можно оставить пустым
                    (prefix, value) -> {
                        // prefix — это название опции ("Режим бега")
                        // value — текущее состояние (true — переключение, false — зажим)

                        if (value) {
                            // Текст, когда выбран режим "Переключение"
                            return Component.literal("ПОШЕЛ ТЫ НАХУЙ: ЗА СПРОСИ ЕБАЛО СЛОМАЮ");
                        } else {
                            // Текст, когда выбран режим "Зажим"
                            return Component.literal("ПОШЕЛ ТЫ НАХУЙ: ПИДР БЛЯТЬ");
                        }
                    },
                    OptionInstance.BOOLEAN_VALUES, // Используем ванильные булевы значения (true/false)
                    this.toggleSprint.get(), // Начальное значение берем из оригинальной настройки
                    // Этот коллбек вызывается, когда игрок кликает по кнопке в меню
                    (newValue) -> {
                        // Важно: синхронизируем значение с оригинальной ванильной опцией,
                        // чтобы игра понимала, какой режим сейчас выбран на самом деле
                        this.toggleSprint.set(newValue);
                    }
            );
        }

        // Подменяем возвращаемое значение метода оригинальной кнопки на нашу кастомную
        cir.setReturnValue(this.customToggleSprintWrapper);
        cir.cancel();
    }
}