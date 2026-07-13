package com.xapc.combat;

import com.xapc.net.Package.TracerBeamPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.xapc.client.render.TracerRenderer.getMuzzleOffset;

public final class HitscanSystem {
    private static final Random RANDOM = new Random();

    private HitscanSystem() {}

    public static List<HitscanResult> fire(ServerPlayer shooter, HitscanSettings settings) {
        List<HitscanResult> results = new ArrayList<>(settings.getPellets());

        for (int i = 0; i < settings.getPellets(); i++) {
            HitscanResult result = fireSinglePellet(shooter, settings);
            results.add(result);
            spawnTracer(shooter, result, settings);
        }
        return results;
    }

    public static List<HitscanResult> trace(ServerPlayer shooter, HitscanSettings settings) {
        List<HitscanResult> results = new ArrayList<>(settings.getPellets());
        for (int i = 0; i < settings.getPellets(); i++) {
            HitscanResult result = traceSinglePellet(shooter, settings); // без hurt()
            results.add(result);
            spawnTracer(shooter, result, settings);
        }
        return results;
    }

    private static HitscanResult fireSinglePellet(ServerPlayer shooter, HitscanSettings settings) {
        Vec3 eyePos = shooter.getEyePosition(1.0F);
        Vec3 lookVec = applySpread(shooter.getViewVector(1.0F), settings.getSpreadDegrees());
        Vec3 endPos = eyePos.add(lookVec.scale(settings.getRange()));

        ClipContext blockClip = new ClipContext(
                eyePos, endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                shooter
        );
        BlockHitResult blockHit = shooter.level().clip(blockClip);
        double maxDistance = blockHit.getType() != HitResult.Type.MISS
                ? eyePos.distanceTo(blockHit.getLocation())
                : settings.getRange();

        Vec3 realEnd = eyePos.add(lookVec.scale(maxDistance));

        Entity hitEntity = findClosestEntityOnRay(shooter, eyePos, realEnd, maxDistance, settings.getRaySize());
        double finalDistance = maxDistance;

        if (hitEntity != null) {
            finalDistance = eyePos.distanceTo(hitEntity.getBoundingBox().getCenter());

            float damage = settings.getDamageAtDistance(finalDistance);
            DamageSource damageSource = shooter.damageSources().playerAttack(shooter);
            hitEntity.hurt(damageSource, damage);

            if (settings.getHitSound() != null) {
                Vec3 hitPos = hitEntity.position();
                shooter.level().playSound(null, hitPos.x, hitPos.y, hitPos.z,
                        settings.getHitSound(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }

        return new HitscanResult(eyePos, realEnd, hitEntity, finalDistance);
    }

    private static HitscanResult traceSinglePellet(ServerPlayer shooter, HitscanSettings settings) {
        Vec3 eyePos = shooter.getEyePosition(1.0F);
        Vec3 lookVec = applySpread(shooter.getViewVector(1.0F), settings.getSpreadDegrees());
        Vec3 endPos = eyePos.add(lookVec.scale(settings.getRange()));

        ClipContext blockClip = new ClipContext(
                eyePos, endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                shooter
        );
        BlockHitResult blockHit = shooter.level().clip(blockClip);
        double maxDistance = blockHit.getType() != HitResult.Type.MISS
                ? eyePos.distanceTo(blockHit.getLocation())
                : settings.getRange();

        Vec3 realEnd = eyePos.add(lookVec.scale(maxDistance));

        Entity hitEntity = findClosestEntityOnRay(shooter, eyePos, realEnd, maxDistance, settings.getRaySize());
        double finalDistance = maxDistance;

        return new HitscanResult(eyePos, realEnd, hitEntity, finalDistance);
    }

    /**
     * Спавнит частицы вдоль луча выстрела — от дула (примерно) до точки попадания.
     * Видно всем игрокам поблизости автоматически, без отдельных пакетов.
     */
    private static void spawnTracer(ServerPlayer shooter, HitscanResult result, HitscanSettings settings) {
        if (!settings.hasBeamTracer()) return;

        Vec3 muzzleOffset = getMuzzleOffset(shooter); // используем базовый lookVec без spread
        Vec3 visualOrigin = result.origin().add(muzzleOffset);

        TracerBeamPacket packet = new TracerBeamPacket(
                visualOrigin.x, visualOrigin.y, visualOrigin.z,
                result.endPoint().x, result.endPoint().y, result.endPoint().z
        );

        for (ServerPlayer p : shooter.level().getServer().getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, packet);
        }
    }
    /**
     * Ручной перебор сущностей вдоль луча — аналог ProjectileUtil.getEntityHitResult,
     * но без привязки к классу Projectile.
     */
    private static Entity findClosestEntityOnRay(ServerPlayer shooter, Vec3 start, Vec3 end, double maxDistance, float raySize) {
        AABB searchBox = shooter.getBoundingBox()
                .expandTowards(end.subtract(start))
                .inflate(1.0D + raySize); // расширяем зону предварительного поиска под толщину луча

        Entity closestEntity = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (Entity entity : shooter.level().getEntities(shooter, searchBox,
                e -> !e.isSpectator() && e.isPickable() && e != shooter)) {

            // раньше: entity.getPickRadius() — теперь дополнительно раздуваем на raySize
            AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius() + raySize);
            var intersection = entityBox.clip(start, end);

            if (intersection.isPresent()) {
                double distanceSq = start.distanceToSqr(intersection.get());
                if (distanceSq < closestDistanceSq && Math.sqrt(distanceSq) <= maxDistance) {
                    closestDistanceSq = distanceSq;
                    closestEntity = entity;
                }
            } else if (entityBox.contains(start)) {
                double distanceSq = 0;
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closestEntity = entity;
                }
            }
        }

        return closestEntity;
    }

    /** Случайное отклонение вектора взгляда в конусе заданного угла (в градусах). */
    private static Vec3 applySpread(Vec3 lookVec, float spreadDegrees) {
        if (spreadDegrees <= 0f) return lookVec;

        double spreadRad = Math.toRadians(spreadDegrees);

        double yaw = (RANDOM.nextDouble() * 2 - 1) * spreadRad;
        double pitch = (RANDOM.nextDouble() * 2 - 1) * spreadRad;

        Vec3 up = Math.abs(lookVec.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 right = lookVec.cross(up).normalize();
        Vec3 trueUp = right.cross(lookVec).normalize();

        Vec3 spreadVec = lookVec
                .add(right.scale(Math.tan(yaw)))
                .add(trueUp.scale(Math.tan(pitch)));

        return spreadVec.normalize();
    }
}