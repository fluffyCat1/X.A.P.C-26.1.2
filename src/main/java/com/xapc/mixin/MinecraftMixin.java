package com.xapc.mixin;

import com.xapc.net.Package.ShootPacket;
import com.xapc.utils.WeaponsAbstractClass;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)

public class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void xapc$cancelAttackAndShoot(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = (Minecraft) (Object) this;
        LocalPlayer player = mc.player;

        if (player != null) {
            ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (mainHandStack.getItem() instanceof WeaponsAbstractClass) {
                ClientPlayNetworking.send(new ShootPacket(InteractionHand.MAIN_HAND));
                cir.setReturnValue(false);
            }
        }
    }
}