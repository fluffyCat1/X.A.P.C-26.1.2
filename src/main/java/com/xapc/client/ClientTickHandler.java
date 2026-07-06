package com.xapc.client;

import com.xapc.client.animations.GenericAnimations;
import com.xapc.client.hud.AmmoHudElement;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class ClientTickHandler {

    private static boolean hudRegistered = false;

    private ClientTickHandler() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
    }
}