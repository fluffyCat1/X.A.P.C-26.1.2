package com.xapc.mixin;

import com.xapc.utils.WeaponClickContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class ContainerMenuPlayerTrackerMixin {

    @Inject(method = "clicked", at = @At("HEAD"))
    private void xapc$start(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        WeaponClickContext.CURRENT_PLAYER.set(player);
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void xapc$end(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        WeaponClickContext.CURRENT_PLAYER.remove();
    }
}