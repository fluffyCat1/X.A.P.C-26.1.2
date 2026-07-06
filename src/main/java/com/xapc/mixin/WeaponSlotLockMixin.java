package com.xapc.mixin;

import com.xapc.utils.WeaponClickContext;
import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class WeaponSlotLockMixin {
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void xapc$blockPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot)(Object)this;
        if (!player.isCreative() && self.getItem().getItem() instanceof WeaponsAbstractClass) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void xapc$blockPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Player player = WeaponClickContext.CURRENT_PLAYER.get();
        if (player != null && player.isCreative()) return;
        if (stack.getItem() instanceof WeaponsAbstractClass) {
            cir.setReturnValue(false);
        }
    }
}