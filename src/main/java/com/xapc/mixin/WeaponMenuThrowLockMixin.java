package com.xapc.mixin;

import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class WeaponMenuThrowLockMixin {

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void xapc$blockThrow(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (player.isCreative()) return;
        AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;

        // Q / Ctrl+Q по слоту с оружием
        if (containerInput == ContainerInput.THROW
                && slotIndex >= 0 && slotIndex < self.slots.size()
                && self.slots.get(slotIndex).getItem().getItem() instanceof WeaponsAbstractClass) {
            ci.cancel();
            return;
        }

        // клик вне окна инвентаря, когда оружие "на курсоре"
        if (slotIndex == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE
                && containerInput == ContainerInput.PICKUP
                && self.getCarried().getItem() instanceof WeaponsAbstractClass) {
            ci.cancel();
        }
    }
}