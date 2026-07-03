package com.xapc.client;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.xapc.client.animations.GenericAnimations;
import com.xapc.client.hud.AmmoHudElement;
import com.xapc.net.Package.AmmoSyncPacket;
import com.xapc.net.Package.AnimTriggerPacket;
import com.xapc.net.Package.PlayerAnimBroadcastPacket;
import com.xapc.utils.WeaponsAbstractClass;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class XapcClient implements ClientModInitializer {
    public static final Identifier BASE_LAYER_ID = Identifier.fromNamespaceAndPath("xapc", "animation");
    private static boolean hudRegistered = false;
    private static final Identifier IDLE_3RD_PLAYER_ID =
            Identifier.fromNamespaceAndPath("xapc", "idle_3rd");

    @Override
    public void onInitializeClient() {

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(BASE_LAYER_ID, 1100,
                player -> new PlayerAnimationController(player,
                        (controller, state, animSetter) -> {
                            if (!controller.isPlayingTriggeredAnimation()
                                    && PlayerAnimResources.hasAnimation(IDLE_3RD_PLAYER_ID)) {
                                RawAnimation idle = RawAnimation.begin()
                                        .then(PlayerAnimResources.getAnimation(IDLE_3RD_PLAYER_ID), Animation.LoopType.LOOP);
                                return animSetter.setAnimation(idle);
                            }
                            return PlayState.STOP;
                        }
                )
        );


        ClientTickEvents.END_CLIENT_TICK.register(client -> {  // <-- сюда
            if (client.level != null) {
                for (Player player : client.level.players()) {
                    GenericAnimations.clientPlayerTick(player);
                }
            }

            if (!hudRegistered && client.player != null) {
                HudElementRegistry.addLast(
                        Identifier.fromNamespaceAndPath("xapc", "ammo_hud"),
                        new AmmoHudElement()
                );
                hudRegistered = true;
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(AmmoSyncPacket.TYPE, (packet, context) -> {
            ClientAmmoStorage.set(packet.playerUuid(), packet.ammo());
        });

        ClientPlayNetworking.registerGlobalReceiver(AnimTriggerPacket.TYPE, (packet, context) -> {
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
        });

        ClientPlayNetworking.registerGlobalReceiver(PlayerAnimBroadcastPacket.TYPE, (packet, context) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            mc.level.players().stream()
                    .filter(p -> p.getUUID().equals(packet.playerUuid()))
                    .findFirst()
                    .ifPresent(player -> {
                        PlayerAnimationController controller = (PlayerAnimationController)
                                PlayerAnimationAccess.getPlayerAnimationLayer(player, BASE_LAYER_ID);
                        if (controller != null) {
                            controller.triggerAnimation(packet.animationId());
                        }
                    });
        });
    }
}