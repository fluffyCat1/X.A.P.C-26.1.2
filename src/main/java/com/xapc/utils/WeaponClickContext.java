package com.xapc.utils;

import net.minecraft.world.entity.player.Player;

public final class WeaponClickContext {
    public static final ThreadLocal<Player> CURRENT_PLAYER = new ThreadLocal<>();

    private WeaponClickContext() {}
}