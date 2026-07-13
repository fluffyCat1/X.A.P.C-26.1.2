package com.xapc.mixin.client;

import com.xapc.utils.GrenadesAbstractClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftReleaseUseMixin {

    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void xapc$preventAutoReleaseForGrenade(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (mainHandStack.getItem() instanceof GrenadesAbstractClass && player.isUsingItem()) {
            if (!mc.options.keyAttack.isDown()) {
                mc.gameMode.releaseUsingItem(player);
            }
            ci.cancel(); // не даём ванильной логике сработать по keyUse и оборвать использование раньше времени
        }
    }
}