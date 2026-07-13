package com.xapc.entity;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GenericGrenadeEntity extends ThrowableItemProjectile implements GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final com.geckolib.animation.RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private static final EntityDataAccessor<Integer> FUSE =
            SynchedEntityData.defineId(GenericGrenadeEntity.class, EntityDataSerializers.INT);

    private static final int DEFAULT_FUSE = 40;

    // ----- параметры физики отскока -----
    private static final double BOUNCE_RESTITUTION = 0.45; // доля скорости, сохраняемая после отскока (0..1)
    private static final double MIN_BOUNCE_SPEED = 0.03;    // ниже этой скорости считаем, что граната легла

    private float horizontalKnockback = 1.96F;       // сила горизонтального импульса в упор
    private float verticalKnockback = 0.67F;         // сила вертикального импульса в упор
    private float knockbackRadiusMultiplier = 4.1F; // радиус = getExplosionPower() * этот множитель

    public GenericGrenadeEntity(EntityType<? extends GenericGrenadeEntity> type, LivingEntity owner, Level level) {
        super(type, level);
        this.setOwner(owner);
    }

    public void setKnockbackStrength(float horizontal, float vertical) {
        this.horizontalKnockback = horizontal;
        this.verticalKnockback = vertical;
    }

    @Override
    protected void updateRotation() {
        // не выравниваем модель по направлению полёта — вместо этого крутим её,
        // имитируя естественное кувыркание брошенного предмета
        float speed = (float) this.getDeltaMovement().length();
        float spinPerTick = 15.0F + speed * 40.0F; // подберите под желаемую скорость вращения

        this.xRotO = this.getXRot();
        this.setXRot(this.getXRot() + spinPerTick);

        this.yRotO = this.getYRot();
        this.setYRot(this.getYRot() + spinPerTick * 0.6F); // чуть медленнее по другой оси — выглядит менее "механически"
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("entity_controller", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FUSE, DEFAULT_FUSE);
    }

    public void setFuse(int ticks) {
        this.entityData.set(FUSE, Math.max(0, ticks));
    }

    public int getFuse() {
        return this.entityData.get(FUSE);
    }

    @Override
    protected Item getDefaultItem() {
        return net.minecraft.world.item.Items.SNOWBALL;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            int fuse = getFuse();
            if (fuse <= 0) {
                explode();
                return;
            }
            setFuse(fuse - 1);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04; // чуть тяжелее ванильного 0.03, подберите под ощущение
    }

    @Override
    protected void onHitEntity(net.minecraft.world.phys.EntityHitResult result) {
        super.onHitEntity(result);
        // граната не взрывается от удара по существу, а продолжает лететь/катиться
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level().isClientSide()) return;

        Vec3 motion = this.getDeltaMovement();
        net.minecraft.core.Direction dir = result.getDirection();
        Vec3 normal = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());

        double dot = motion.dot(normal);
        Vec3 reflected = motion.subtract(normal.scale(2 * dot));
        Vec3 bounced = reflected.scale(BOUNCE_RESTITUTION);

        if (bounced.length() < MIN_BOUNCE_SPEED) {
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            this.setDeltaMovement(bounced);
            playBounceSound();
        }
    }

    private void playBounceSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.STONE_BREAK,
                SoundSource.PLAYERS, 0.4F, 1.2F + (this.random.nextFloat() - 0.5F) * 0.3F);
    }

    protected float getExplosionPower() {
        return 2.2F;
    }

    private void explode() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.explode(
                    this,
                    this.getX(), this.getY(), this.getZ(),
                    getExplosionPower(),
                    false,
                    Level.ExplosionInteraction.NONE // отключаем ванильный урон/knockback — делаем всё сами ниже
            );

            applyGrenadeDamageAndKnockback(serverLevel);

            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0, 0, 0, 0);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 1.0F);
        }
        this.discard();
    }

    private void applyGrenadeDamageAndKnockback(ServerLevel serverLevel) {
        double radius = getExplosionPower() * knockbackRadiusMultiplier;
        AABB area = this.getBoundingBox().inflate(radius);

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive);

        for (LivingEntity entity : targets) {
            double dx = entity.getX() - this.getX();
            double dy = (entity.getY() + entity.getBbHeight() * 0.5) - this.getY();
            double dz = entity.getZ() - this.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > radius) continue;

            if (distance < 0.05) {
                dx = 0; dz = 0;
                distance = 1.0;
            }

            double falloff = 1.0 - (distance / radius);
            falloff = falloff * falloff;

            double invDist = 1.0 / distance;
            double nx = dx * invDist;
            double nz = dz * invDist;

            double horizontal = falloff * horizontalKnockback;
            double vertical = falloff * verticalKnockback + 0.15;

            Vec3 push = new Vec3(nx * horizontal, vertical, nz * horizontal);

            // урон — считаем сами, раз ванильный взрыв отключён
            float damage = (float) (getExplosionDamage() * falloff);
            if (damage > 0.5F) {
                entity.hurtServer(serverLevel, this.damageSources().explosion(this, this.getOwner()), damage);
            }

            // knockback — напрямую в deltaMovement, БЕЗ учёта knockback_resistance
            entity.setDeltaMovement(entity.getDeltaMovement().add(push));
            entity.fallDistance = 0;

            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
        }
    }

    protected float getExplosionDamage() {
        return 40.0F; // максимальный урон в упор, подберите под баланс
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }
}