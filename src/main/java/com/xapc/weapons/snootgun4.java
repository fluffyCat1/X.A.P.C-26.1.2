package com.xapc.weapons;

import com.xapc.utils.WeaponsAbstractClass;

public class snootgun4 extends WeaponsAbstractClass {

    public snootgun4(Properties properties) {
        super(properties);
    }

    @Override
    public int equipAnimationDurationTick() {
        return 15;
    }

    @Override
    public int getMaxAmmo() {
        return 2;
    }

    @Override
    public float getDamage() {
        return 12.5F;
    }

    @Override
    public int reloadDelay() {
        return 17;
    }

    @Override
    public int shootAnimationDurationTick() {
        return 10;
    }

    @Override
    public int reloadAnimationDurationTick() {
        return 38;
    }

    @Override
    public net.minecraft.resources.Identifier getIdleAnimationId() {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath("xapc", "idle");
    }

    @Override
    public net.minecraft.resources.Identifier getShootAnimationId() {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath("xapc", "shoot");
    }

    @Override
    public net.minecraft.resources.Identifier getReloadAnimationId() {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath("xapc", "reload");
    }
}
