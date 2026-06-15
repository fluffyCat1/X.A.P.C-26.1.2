package com.xapc.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {

    @Shadow @Final public static Identifier MOJANG_STUDIOS_LOGO_LOCATION;

    // 1. Делаем фон темно-серым/черным
    @Inject(method = "replaceAlpha", at = @At("HEAD"), cancellable = true)
    private static void onReplaceAlpha(int color, int alpha, CallbackInfoReturnable<Integer> circles) {
        circles.setReturnValue(ARGB.color(alpha, 16, 16, 16));
        circles.cancel();
    }

    // 2. Полный перехват отрисовки логотипа
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void onExtractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        int centerX = width / 2;
        int centerY = height / 2;

        // Рассчитываем пропорции под твою картинку 1024x324 (примерно 3.16 к 1)
        int logoWidth = 360;  // Желаемая ширина на экране
        int logoHeight = 114; // Желаемая высота на экране (сохраняем аспектное соотношение)

        int x = centerX - (logoWidth / 2);
        int y = centerY - (logoHeight / 2) - 20; // Слегка приподнимем над полоской загрузки

        // Сначала заливаем весь экран цветом фона
        graphics.fill(0, 0, width, height, ARGB.color(255, 16, 16, 16));

        // Используем универсальный blit с float UV-координатами.
        // Читаем текстуру от 0.0 до 1.0 по обеим осям — это заставит движок нарисовать весь прямоугольник PNG «как есть».
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                MOJANG_STUDIOS_LOGO_LOCATION,
                x, y,                         // Левый верхний угол на экране
                (float) logoWidth, (float) logoHeight,        // Размеры отрисовки на экране
                (int) 0.0F, (int) 0.0F,                   // Начало текстуры (U, V) -> верхний левый угол (0%, 0%)
                (int) 1.0F, (int) 1.0F,                   // Конец текстуры (U, V) -> правый нижний угол (100%, 100%)
                ARGB.color(255, 255, 255, 255) // Белый фильтр, цвета пикселей не изменятся
        );

        // Рисуем рамку и полоску прогресс-бара загрузки
        int barWidth = 120;
        int barHeight = 10;
        int barX = centerX - (barWidth / 2);
        int barY = (int)((double)height * 0.8325) - (barHeight / 2);

        // Белая рамка
        graphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, ARGB.color(255, 255, 255, 255));
        // Темная внутренность
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, ARGB.color(255, 16, 16, 16));

        // Гасим оригинальный метод
        ci.cancel();
    }
}