package com.xapc.client.net;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.manager.AnimatableManager;
import com.xapc.client.ClientEquipLock;
import com.xapc.client.ClientGrenadeCooldown;
import com.xapc.net.Package.AnimTriggerPacket;
import com.xapc.utils.GrenadesAbstractClass;
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
                    if (!(stack.getItem() instanceof GeoItem geoItem)) {
                        System.out.println("[grenade-debug] item is not GeoItem: " + stack.getItem());
                        return;
                    }

                    // клиентские таймеры equip-lock и grenade-cooldown нужны только
                    // для локального игрока — именно его ввод перехватывает MinecraftMixin
                    boolean isLocalPlayer = owner == mc.player;

                    if (isLocalPlayer && !packet.stop()) {
                        if ("equip".equals(packet.animName()) && "base_controller".equals(packet.controllerName())) {
                            if (stack.getItem() instanceof GrenadesAbstractClass grenade) {
                                ClientEquipLock.start(owner.getUUID(), grenade.equipAnimationDurationTick());
                            } else if (stack.getItem() instanceof WeaponsAbstractClass weapon) {
                                ClientEquipLock.start(owner.getUUID(), weapon.equipAnimationDurationTick());
                            }
                        }

                        if ("launch".equals(packet.animName()) && "base_controller".equals(packet.controllerName())
                                && stack.getItem() instanceof GrenadesAbstractClass grenade) {
                            ClientGrenadeCooldown.start(owner.getUUID(), grenade.getThrowCooldownTicks());
                        }
                    }

                    AnimatableManager<GeoAnimatable> manager =
                            geoItem.getAnimatableInstanceCache().getManagerForId(packet.instanceId());

                    System.out.println("[grenade-debug] received anim=" + packet.animName()
                            + " controller=" + packet.controllerName()
                            + " stop=" + packet.stop()
                            + " instanceId=" + packet.instanceId()
                            + " managerFound=" + (manager != null));

                    if (manager == null) return;

                    if (packet.stop()) {
                        manager.stopTriggeredAnimation(packet.controllerName(), packet.animName());
                    } else {
                        manager.tryTriggerAnimation(packet.controllerName(), packet.animName());
                    }
                });
    }
}