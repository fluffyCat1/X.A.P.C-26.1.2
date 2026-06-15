package com.xapc.utils;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.ClientUtil;
import com.geckolib.util.GeckoLibUtil;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.xapc.client.render.GenericWeaponRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class WeaponsAbstractClass extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static boolean playingAnimation;
    // Сделал public, чтобы рендерер (GenericWeaponRenderer) мог видеть эти анимации для проверок
    public static final RawAnimation IDLE_FPS = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation IDLE_3RD = RawAnimation.begin().thenLoop("idle_3rd");
    public static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
    public static final RawAnimation RELOAD = RawAnimation.begin().thenPlay("reload");

    public WeaponsAbstractClass(Properties properties) {
        super(properties);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            // Исправлено: передаем лямбду, которая создает рендерер и прокидывает ссылку на класс оружия
            private final Supplier<GenericWeaponRenderer> renderer = Suppliers.memoize(() ->
                    new GenericWeaponRenderer(WeaponsAbstractClass.this)
            );

            @Override
            @Nullable
            public GeoItemRenderer<WeaponsAbstractClass> getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("weapon_controller", 20, state -> {

            return PlayState.CONTINUE;
                }).receiveTriggeredAnimations()
                .triggerableAnim("shoot", SHOOT)
                .triggerableAnim("idle", IDLE_FPS)
        );
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "weapon_controller", "shoot");
        }

        return super.use(level, player, hand);
    }

    // если ты не знаешь как это работает то даже не пытайся разобраться я сам хз
    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @org.jspecify.annotations.Nullable EquipmentSlot slot) {
        if (owner instanceof ServerPlayer player) {
            ItemStack mainHandStack = player.getItemBySlot(EquipmentSlot.MAINHAND);
            long animId = GeoItem.getOrAssignId(itemStack, level);

            if (mainHandStack != itemStack) {
                triggerAnim(player, animId, "weapon_controller", "idle");
            }
        }
        super.inventoryTick(itemStack, level, owner, slot);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public abstract int getMaxAmmo();
    public abstract float getDamage();

    public abstract net.minecraft.resources.Identifier getIdleAnimationId();
    public abstract net.minecraft.resources.Identifier getShootAnimationId();
    public abstract net.minecraft.resources.Identifier getReloadAnimationId();
}