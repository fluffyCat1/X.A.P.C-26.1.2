package com.xapc.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record WeaponKey(UUID player, String weaponId) {
    public static WeaponKey of(Player player, ItemStack stack) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .toString(); // например "xapc:ak47"
        return new WeaponKey(player.getUUID(), id);
    }
}