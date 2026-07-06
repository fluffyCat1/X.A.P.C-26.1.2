package com.xapc.weapons;

import com.xapc.sound.ModSounds;
import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.sounds.SoundEvent;

public class snootgun4 extends WeaponsAbstractClass {
    private static final java.util.Random EQUIP_RANDOM = new java.util.Random();

    public snootgun4(Properties properties) {
        super(properties);
    }

    @Override
    protected String chooseEquipAnimKey() {
        return EQUIP_RANDOM.nextInt(100) < 90 ? "equip" : "equip2";
    }

    @Override
    public int equipAnimationDurationTick() {
        return 17;
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
        return 15;
    }

    @Override
    public int shootAnimationDurationTick() {
        return 13;
    }

    @Override
    public int reloadAnimationDurationTick() {
        return 40;
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

    @Override
    public net.minecraft.resources.Identifier getEquipAnimationId() {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath("xapc", "equip");
    }

    @Override
    public SoundEvent getShootSound() {
        return ModSounds.SHOOTGUN4_SHOOT;
    }

    @Override
    public SoundEvent getReloadSound() {
        return ModSounds.SHOOTGUN4_RELOAD;
    }
}