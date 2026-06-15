package com.xapc.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xapc.client.animations.GenericAnimations;
import com.xapc.net.NetWorking;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import static com.xapc.item.ItemRegistry.SHOOTGUN4;

//import static com.xapc.item.ItemRegistry.SHOOTGUN4;


public class XapcClient implements ClientModInitializer {
    public static final Identifier BASE_LAYER_ID = Identifier.fromNamespaceAndPath("xapc", "animation");

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
        });

//       ClientTickEvents.END_CLIENT_TICK.register(client -> {
//            if (client.player != null) {
//                // Проверяем наличие дробовика в основной или левой руке
//                boolean holdsShotgun = client.player.getItemBySlot(EquipmentSlot.MAINHAND).is(SHOOTGUN4)
//                        || client.player.getItemBySlot(EquipmentSlot.OFFHAND).is(SHOOTGUN4);
//                // Передаем игрока и статус удержания оружия в наш обновленный менеджер анимаций
//                    RegisterAnimations.handleIdleAnimation(client.player, holdsShotgun);
//            }
//        });

    }
}