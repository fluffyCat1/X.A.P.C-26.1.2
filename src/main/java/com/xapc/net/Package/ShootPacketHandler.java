package com.xapc.net.Package;

import com.xapc.utils.WeaponKey;
import com.xapc.utils.WeaponsAbstractClass;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static com.xapc.utils.WeaponsAbstractClass.*;

public final class ShootPacketHandler {

    private ShootPacketHandler() {}

    public static void handle(ShootPacket payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        context.server().execute(() -> {
            ItemStack stack = player.getItemInHand(payload.hand());
            if (!(stack.getItem() instanceof WeaponsAbstractClass weapon)) return;

            WeaponKey key = WeaponKey.of(player, stack);

            if (shootTicksMap.getOrDefault(key, 0) > 0) return;
            if (equipTicksMap.getOrDefault(player.getUUID(), 0) > 0) return;

            int ammo = getAmmo(key, weapon.getMaxAmmo());
            if (ammo <= 0) return;

            ServerLevel level = (ServerLevel) player.level();
            long animId = instanceIdFor(player.getUUID());

            reloadTicksMap.put(key, 0);
            stopAnimForPlayer(player, animId, "base_controller", "reload");
            broadcastStopAnimTrigger(player, animId, "third_person_controller", "reload_3rd");

            setAmmo(key, ammo - 1);
            triggerAnimForPlayer(player, animId, "base_controller", "shoot");
            broadcastAnimTrigger(player, animId, "third_person_controller", "shoot_3rd");
            broadcastPlayerAnim(player, weapon.getShootAnimationId());
            shootTicksMap.put(key, weapon.shootAnimationDurationTick());

            playWeaponSound(player, weapon.getShootSound(), weapon.getShootVolume(), weapon.getShootPitch());
        });
    }
}