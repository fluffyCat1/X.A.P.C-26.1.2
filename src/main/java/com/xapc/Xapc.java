package com.xapc;

import com.geckolib.animatable.GeoItem;
import com.xapc.item.ItemRegistry;
import com.xapc.net.NetWorking;
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

import static com.xapc.utils.WeaponsAbstractClass.reloadTicksMap;
import static com.xapc.utils.WeaponsAbstractClass.shootTicksMap;

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
                long animId = GeoItem.getOrAssignId(stack, level);

                // прерываем перезарядку
                reloadTicksMap.put(player.getUUID(), 0);
                weapon.stopTriggeredAnim(player, animId, "base_controller", "reload");

                // стреляем
                WeaponsAbstractClass.setAmmo(player.getUUID(), ammo - 1);
                weapon.triggerAnim(player, animId, "base_controller", "shoot");
                shootTicksMap.put(player.getUUID(), weapon.shootAnimationDurationTick());
            });
        });
    }
}