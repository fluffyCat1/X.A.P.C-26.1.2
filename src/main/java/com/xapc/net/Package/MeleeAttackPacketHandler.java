package com.xapc.net.Package;

import com.xapc.utils.MeleeAttackCapable;
import com.xapc.utils.WeaponKey;
import com.xapc.utils.WeaponsAbstractClass;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static com.xapc.utils.WeaponsAbstractClass.*;

public final class MeleeAttackPacketHandler {

    private MeleeAttackPacketHandler() {}

    public static void handle(MeleeAttackPacket payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        context.server().execute(() -> {
            ItemStack stack = player.getItemInHand(payload.hand());

            // если у оружия нет удара прикладом — просто выходим, никакой ошибки
            if (!(stack.getItem() instanceof MeleeAttackCapable melee)) return;
            if (!(stack.getItem() instanceof WeaponsAbstractClass weapon)) return;

            WeaponKey key = WeaponKey.of(player, stack);

            // нельзя бить прикладом во время стрельбы/перезарядки
            if (shootTicksMap.getOrDefault(key, 0) > 0) return;
            if (reloadTicksMap.getOrDefault(key, 0) > 0) return;

            long animId = instanceIdFor(player.getUUID());

            triggerAnimForPlayer(player, animId, "base_controller", "melee");
            broadcastAnimTrigger(player, animId, "third_person_controller", "melee_3rd");

            playWeaponSound(player, melee.getMeleeSound(), melee.getMeleeVolume(), melee.getMeleePitch());

            // тут — рейкаст/хитскан перед игроком и applyDamage(melee.getMeleeDamage())
        });
    }
}