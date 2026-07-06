package com.xapc.mixin;

import com.mojang.logging.LogUtils;
import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class WeaponDropSafetyNetMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"), cancellable = true
    )
    private void xapc$returnWeaponInsteadOfDrop(ItemStack itemStack, boolean randomly, boolean thrownFromHand, CallbackInfoReturnable<ItemEntity> cir) {
        LOGGER.info("[xapc] drop() called, item={}, isWeapon={}", itemStack.getItem(), itemStack.getItem() instanceof WeaponsAbstractClass);

        if (!(itemStack.getItem() instanceof WeaponsAbstractClass)) return;

        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof Player player)) {
            LOGGER.info("[xapc] not a player, skipping");
            return;
        }
        if (player.isCreative()) {
            LOGGER.info("[xapc] creative, skipping");
            return;
        }

        boolean added = player.getInventory().add(itemStack);
        LOGGER.info("[xapc] inventory.add returned {}", added);

        if (added) {
            cir.setReturnValue(null);
            LOGGER.info("[xapc] cancelled, item returned to inventory");
        }
    }
}