package com.xapc.utils;

import com.xapc.combat.HitscanSettings;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public interface MeleeAttackCapable {
    float getMeleeDamage();
    int meleeAnimationDurationTick();
    Identifier getMeleeAnimationId();
    SoundEvent getMeleeSound();
    HitscanSettings getMeleeHitscanSettings();
    SoundEvent getMeleeHitSound();

    int meleeWindupTicks(); // <-- сколько тиков ждать от старта анимации до урона

    default float getMeleeVolume() { return 1.0F; }
    default float getMeleePitch()  { return 1.0F; }
}