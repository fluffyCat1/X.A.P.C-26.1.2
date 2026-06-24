package com.xapc.client;

import com.xapc.client.animations.GenericAnimations;
import com.xapc.client.hud.AmmoHudElement;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;


public class XapcClient implements ClientModInitializer {
    public static final Identifier BASE_LAYER_ID = Identifier.fromNamespaceAndPath("xapc", "animation");
    private static boolean hudRegistered = false;

    @Override
    public void onInitializeClient() {

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(BASE_LAYER_ID, 1100,
                player -> new PlayerAnimationController(player,
                        (controller, state, animSetter) -> PlayState.STOP
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null) {
                // Пробегаемся по ВСЕМ игрокам, которых наш клиент сейчас видит в мире
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
    }
}