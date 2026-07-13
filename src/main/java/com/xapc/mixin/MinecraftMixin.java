package com.xapc.mixin;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.xapc.client.ClientAmmoStorage;
import com.xapc.client.ClientEquipLock;
import com.xapc.client.ClientGrenadeCooldown;
import com.xapc.client.ClientShootCooldown;
import com.xapc.net.Package.GrenadeStartCookPacket;
import com.xapc.net.Package.ShootPacket;
import com.xapc.utils.GrenadesAbstractClass;
import com.xapc.utils.WeaponsAbstractClass;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
        if (player == null) return;

        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (mainHandStack.getItem() instanceof WeaponsAbstractClass weapon) {
            if (ClientEquipLock.isLocked(player.getUUID())) {
                cir.setReturnValue(false);
                return;
            }

            int ammo = ClientAmmoStorage.get(player.getUUID(), weapon.getMaxAmmo());
            if (ammo <= 0) {
                cir.setReturnValue(false);
                return;
            }

            if (!ClientShootCooldown.isReady(player.getUUID())) {
                cir.setReturnValue(false);
                return;
            }

            long animId = WeaponsAbstractClass.instanceIdFor(player.getUUID());
            AnimatableManager<GeoAnimatable> manager =
                    weapon.getAnimatableInstanceCache().getManagerForId(animId);
            if (manager != null) {
                manager.tryTriggerAnimation("base_controller", "shoot");
            }

            ClientShootCooldown.start(player.getUUID(), weapon.shootAnimationDurationTick());

            ClientPlayNetworking.send(new ShootPacket(InteractionHand.MAIN_HAND));
            cir.setReturnValue(false);
            return;
        }

        if (mainHandStack.getItem() instanceof GrenadesAbstractClass) {
            cir.setReturnValue(false);

            if (player.isUsingItem()) return;
            if (ClientEquipLock.isLocked(player.getUUID())) return;
            if (ClientGrenadeCooldown.isOnCooldown(player.getUUID())) return;

            ClientPlayNetworking.send(new GrenadeStartCookPacket(InteractionHand.MAIN_HAND));
            player.startUsingItem(InteractionHand.MAIN_HAND);

            if (!player.isUsingItem()) {
                ClientPlayNetworking.send(new GrenadeStartCookPacket(InteractionHand.MAIN_HAND));
                player.startUsingItem(InteractionHand.MAIN_HAND); // локальное предсказание, таймер держится, пока зажат ЛКМ
            }
        }
    }
}