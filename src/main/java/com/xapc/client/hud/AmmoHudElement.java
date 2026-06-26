package com.xapc.client.hud;

import com.xapc.client.ClientAmmoStorage;
import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

public class AmmoHudElement implements HudElement {
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui) return;

        ItemStack stack = mc.player.getMainHandItem();

        if (!(stack.getItem() instanceof WeaponsAbstractClass weapon)) return;

        int ammo = ClientAmmoStorage.get(mc.player.getUUID(), weapon.getMaxAmmo());
        String text = ammo + " / " + weapon.getMaxAmmo();

        int textWidth = mc.font.width(text);
        int x = graphics.guiWidth() - textWidth - 10;
        int y = graphics.guiHeight() - 50;

        graphics.fill(x - 5, y - 5, x + textWidth + 5, y + 15, 0x80000000); // синий

        graphics.nextStratum();

        graphics.text(mc.font, text, x, y, 0xFFFFFFFF, false);
    }
}