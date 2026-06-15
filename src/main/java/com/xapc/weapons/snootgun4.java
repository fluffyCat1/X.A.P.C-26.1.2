package com.xapc.weapons;

import com.xapc.utils.WeaponsAbstractClass;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import net.minecraft.resources.Identifier;

public class snootgun4 extends WeaponsAbstractClass {

    public snootgun4(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxAmmo() {
        return 0;
    }

    @Override
    public float getDamage() {
        return 0;
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
