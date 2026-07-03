package com.xapc;

import com.geckolib.animatable.GeoItem;
import com.xapc.item.ItemRegistry;
import com.xapc.net.NetWorking;
import com.xapc.net.Package.AmmoSyncPacket;
import com.xapc.net.Package.AnimTriggerPacket;
import com.xapc.net.Package.PlayerAnimBroadcastPacket;
import com.xapc.net.Package.ShootPacket;
import com.xapc.utils.WeaponsAbstractClass;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xapc.utils.WeaponsAbstractClass.*;

public class Xapc implements ModInitializer {
    public static final String MOD_ID = "xapc";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");
        ItemRegistry.initialize();
        ShootPacket.register();
        PayloadTypeRegistry.clientboundPlay().register(AmmoSyncPacket.TYPE, AmmoSyncPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AnimTriggerPacket.TYPE, AnimTriggerPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerAnimBroadcastPacket.TYPE, PlayerAnimBroadcastPacket.CODEC);


        ServerPlayNetworking.registerGlobalReceiver(ShootPacket.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                ItemStack stack = player.getItemInHand(payload.hand());
                if (!(stack.getItem() instanceof WeaponsAbstractClass weapon)) return;

                // проверки блокировки — до любых изменений состояния
                if (shootTicksMap.getOrDefault(player.getUUID(), 0) > 0) return;
                if (WeaponsAbstractClass.equipTicksMap.getOrDefault(player.getUUID(), 0) > 0) return;

                int ammo = WeaponsAbstractClass.getAmmo(player.getUUID(), weapon.getMaxAmmo());
                if (ammo <= 0) return;

                ServerLevel level = (ServerLevel) player.level();
                long animId = WeaponsAbstractClass.instanceIdFor(player.getUUID());

                reloadTicksMap.put(player.getUUID(), 0);
                stopAnimForPlayer(player, animId, "base_controller", "reload");                    // локально
                broadcastStopAnimTrigger(player, animId, "third_person_controller", "reload_3rd"); // всем

                WeaponsAbstractClass.setAmmo(player.getUUID(), ammo - 1);
                triggerAnimForPlayer(player, animId, "base_controller", "shoot");           // локально
                broadcastAnimTrigger(player, animId, "third_person_controller", "shoot_3rd");   // всем
                broadcastPlayerAnim(player, weapon.getShootAnimationId());
                shootTicksMap.put(player.getUUID(), weapon.shootAnimationDurationTick());
            });
        });
    }
}