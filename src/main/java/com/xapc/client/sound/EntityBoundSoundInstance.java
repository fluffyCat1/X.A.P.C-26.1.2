package com.xapc.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class EntityBoundSoundInstance extends AbstractTickableSoundInstance {
    private final Entity entity;
    private boolean finished = false;

    public EntityBoundSoundInstance(SoundEvent sound, SoundSource source, Entity entity, float volume, float pitch) {
        super(sound, source, RandomSource.create());
        this.entity = entity;
        this.volume = volume;
        this.pitch = pitch;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.looping = false;
        this.attenuation = SoundInstance.Attenuation.LINEAR; // <-- добавлено: затухание по расстоянию
    }

    @Override
    public void tick() {
        if (!entity.isAlive()) {
            this.finished = true;
            return;
        }
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public boolean isStopped() {
        return finished || super.isStopped();
    }
}