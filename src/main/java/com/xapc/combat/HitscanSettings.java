package com.xapc.combat;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;

public final class HitscanSettings {
    private final double range;
    private final float spreadDegrees;
    private final int pellets;
    private final float damage;
    private final float damageFalloffStart;
    private final float minDamageMultiplier;
    private final boolean beamTracer;
    private final float raySize; // <-- новое поле
    private final SoundEvent hitSound;

    public SoundEvent getHitSound() {
        return hitSound;
    }

    public HitscanSettings(Builder b, SoundEvent hitSound) {
        this.range = b.range;
        this.spreadDegrees = b.spreadDegrees;
        this.pellets = b.pellets;
        this.damage = b.damage;
        this.damageFalloffStart = b.damageFalloffStart;
        this.minDamageMultiplier = b.minDamageMultiplier;
        this.beamTracer = b.beamTracer;
        this.raySize = b.raySize;
        this.hitSound = hitSound;
    }


    public double getRange() { return range; }
    public float getSpreadDegrees() { return spreadDegrees; }
    public int getPellets() { return pellets; }
    public float getBaseDamage() { return damage; }
    public float getDamageFalloffStart() { return damageFalloffStart; }
    public float getMinDamageMultiplier() { return minDamageMultiplier; }
    public boolean hasBeamTracer() { return beamTracer; }
    public float getRaySize() { return raySize; }

    public float getDamageAtDistance(double distance) {
        if (distance <= damageFalloffStart) return damage;
        double falloffRange = range - damageFalloffStart;
        if (falloffRange <= 0) return damage * minDamageMultiplier;
        double t = Math.min(1.0, (distance - damageFalloffStart) / falloffRange);
        float multiplier = (float) (1.0 - t * (1.0 - minDamageMultiplier));
        return damage * multiplier;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private SoundEvent hitSound;
        private float raySize = 0f;
        private double range = 50.0;
        private float spreadDegrees = 0f;
        private int pellets = 1;
        private float damage = 10f;
        private float damageFalloffStart = -1;
        private float minDamageMultiplier = 1.0f;
        private ParticleOptions tracerParticle = null;
        private double tracerParticleSpacing = 0.5;
        private int tracerMaxPoints = 20;
        private boolean beamTracer = false;

        public Builder range(double range) { this.range = range; return this; }
        public Builder spreadDegrees(float spread) { this.spreadDegrees = spread; return this; }
        public Builder pellets(int pellets) { this.pellets = pellets; return this; }
        public Builder damage(float damage) { this.damage = damage; return this; }
        public Builder hitSound(SoundEvent sound) {
            this.hitSound = sound;
            return this;
        }

        public Builder damageFalloff(float startDistance, float minMultiplier) {
            this.damageFalloffStart = startDistance;
            this.minDamageMultiplier = minMultiplier;
            return this;
        }

        public Builder raySize(float raySize) {
            this.raySize = raySize;
            return this;
        }

        public Builder tracerParticle(ParticleOptions particle) {
            this.tracerParticle = particle;
            return this;
        }

        public Builder tracerParticleSpacing(double spacing) {
            this.tracerParticleSpacing = spacing;
            return this;
        }

        public Builder tracerMaxPoints(int max) {
            this.tracerMaxPoints = max;
            return this;
        }

        /** Включает рендер трассера как линии (без партиклов) через пакет TracerBeamPacket. */
        public Builder beamTracer(boolean enabled) {
            this.beamTracer = enabled;
            return this;
        }

        public HitscanSettings build() {
            if (damageFalloffStart < 0) damageFalloffStart = (float) range;
            return new HitscanSettings(this, hitSound);
        }
    }
}