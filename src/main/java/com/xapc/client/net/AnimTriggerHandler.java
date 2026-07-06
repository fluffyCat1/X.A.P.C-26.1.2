package com.xapc.client.net;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.xapc.net.Package.AnimTriggerPacket;
import com.xapc.utils.WeaponsAbstractClass;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public final class AnimTriggerHandler {

    private AnimTriggerHandler() {}

    public static void handle(AnimTriggerPacket packet, ClientPlayNetworking.Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        mc.level.players().stream()
                .filter(p -> p.getUUID().equals(packet.playerUuid()))
                .findFirst()
                .ifPresent(owner -> {
                    ItemStack stack = owner.getMainHandItem();
                    if (!(stack.getItem() instanceof WeaponsAbstractClass weapon)) return;

                    AnimatableManager<GeoAnimatable> manager =
                            weapon.getAnimatableInstanceCache().getManagerForId(packet.instanceId());
                    if (manager == null) return;

                    if (packet.stop()) {
                        manager.stopTriggeredAnimation(packet.controllerName(), packet.animName());
                    } else {
                        manager.tryTriggerAnimation(packet.controllerName(), packet.animName());
                    }
                });
    }
}