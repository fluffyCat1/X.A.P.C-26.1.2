package com.xapc.mixin;

import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class WeaponDropLockMixin {

    @Inject(method = "drop(Z)V", at = @At("HEAD"), cancellable = true)
    private void xapc$blockWeaponDropAll(boolean all, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        ItemStack selected = self.getInventory().getSelectedItem();
        if (selected.getItem() instanceof WeaponsAbstractClass && !self.isCreative()) {
            ci.cancel();
            self.containerMenu.broadcastFullState();
        }
    }
}