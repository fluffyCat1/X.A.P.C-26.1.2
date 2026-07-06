package com.xapc.mixin;

import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class WeaponHandLockMixin {
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void xapc$blockHandChange(EquipmentSlot slot, ItemStack newStack, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self instanceof Player p && p.isCreative()) return;
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) return;

        ItemStack current = self.getItemBySlot(slot);
        boolean losingWeapon = current.getItem() instanceof WeaponsAbstractClass
                && !ItemStack.matches(current, newStack);
        boolean gainingInOffhand = slot == EquipmentSlot.OFFHAND
                && newStack.getItem() instanceof WeaponsAbstractClass;

        if (losingWeapon || gainingInOffhand) {
            ci.cancel();
        }
    }
}
