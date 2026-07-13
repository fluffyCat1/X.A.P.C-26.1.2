package com.xapc.weapons;

import com.xapc.entity.GenericGrenadeEntity;
import com.xapc.registry.ModEntities;
import com.xapc.utils.GrenadesAbstractClass;
import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class granade extends GrenadesAbstractClass {
    public granade(Properties properties) {
        super(properties);
    }

    @Override
    protected void spawnGrenadeEntity(ServerPlayer player, ItemStack stack, int fuseTicks, float power) {
        GenericGrenadeEntity grenade = new GenericGrenadeEntity(ModEntities.GENERIC_GRENADE, player, player.level());
        grenade.setFuse(fuseTicks);
        grenade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

        Vec3 look = player.getLookAngle();

        // для слабых бросков (мягкий подброс) добавляем наклон вниз,
        // чтобы граната падала недалеко, а не летела по прямой линии взгляда
        double pitchBias = power < 1.0F ? -0.35 : 0.0;
        Vec3 tossDir = new Vec3(look.x, look.y + pitchBias, look.z).normalize();

        grenade.shoot(tossDir.x, tossDir.y, tossDir.z, power, 0.0F);

        player.level().addFreshEntity(grenade);
    }

    @Override
    public int softTossFuseTicks() {
        return 30; // 1.5с — фиксированное время до взрыва при подбросе, без варьирования
    }

    @Override
    public int softTossAnimationDurationTick() {
        return launchAnimationDurationTick(); // можно оставить ту же длительность, что и у launch
    }

    @Override
    public SoundEvent getPinPullSound() {
        return null;
    }

    @Override
    public SoundEvent getLaunchSound() {
        return null;
    }

    @Override
    public int getMaxGrenades() {
        return 4;
    }

    @Override
    public int getRegenIntervalTicks() {
        return 60;
    }

    @Override
    public int equipAnimationDurationTick() {
        return 13;
    }

    @Override
    public int launchAnimationDurationTick() {
        return 6;
    }

    @Override
    public Identifier getEquipAnimationId() {
        return Identifier.fromNamespaceAndPath("xapc", "empty"); // заглушка, лишь бы не null
    }

    @Override
    public Identifier getIdleAnimationId() {
        return Identifier.fromNamespaceAndPath("xapc", "empty");
    }

    @Override
    public Identifier getLaunchAnimationId() {
        return Identifier.fromNamespaceAndPath("xapc", "empty");
    }

    @Override
    public int baseFuseTicks() {
        return 30; // 1.5с — короткое нажатие ЛКМ
    }

    @Override
    public int getThrowCooldownTicks() {
        return 15;
    }

    @Override
    public int minFuseTicks() {
        return 10; // 0.5с — полное закукивание
    }
}
