package com.xapc.weapons;

import com.xapc.combat.HitscanSettings;
import com.xapc.sound.ModSounds;
import com.xapc.utils.MeleeAttackCapable;
import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class snootgun4 extends WeaponsAbstractClass implements MeleeAttackCapable {
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
    public int reloadDelay() {
        return 15;
    }

    @Override
    public int shootAnimationDurationTick() {
        return 15;
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

    private static final HitscanSettings HITSCAN = HitscanSettings.builder()
            .range(80.0)
            .spreadDegrees(5.8f)
            .pellets(12)
            .damage(9.5f)
            .damageFalloff(40f, 0.5f)
            .beamTracer(true)
            .raySize(0.03f)
            .hitSound(ModSounds.HITSOUND) // <-- звук попадания пули
            .build();

    @Override
    public HitscanSettings getHitscanSettings() {
        return HITSCAN;
    }

    @Override
    public float getMeleeDamage() {
        return 10.0F;
    }

    @Override
    public int meleeAnimationDurationTick() {
        return 20;
    }

    @Override
    public net.minecraft.resources.Identifier getMeleeAnimationId() {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath("xapc", "buttstroke");
    }

    @Override
    public SoundEvent getMeleeSound() {
        return ModSounds.BUTTSTROKE;
    }

    @Override
    public SoundEvent getMeleeHitSound() {
        return ModSounds.HIT_MELEE; // добавь этот звук в ModSounds
    }

    @Override
    public SoundEvent getHitSound() {
        return ModSounds.HITSOUND; // добавь этот звук в ModSounds
    }

    @Override
    public HitscanSettings getMeleeHitscanSettings() {
        return HitscanSettings.builder()
                .range(2.0)
                .spreadDegrees(0f)
                .pellets(1)
                .damage(6f)
                .raySize(0.5f)   // <-- луч толщиной ~0.3 блока, легче попасть по цели
                .beamTracer(false)
                .build();
    }

    @Override
    public int meleeWindupTicks() {
        return 5; // ~0.2 сек задержки перед уроном
    }
}