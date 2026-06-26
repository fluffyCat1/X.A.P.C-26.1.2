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
import com.xapc.net.Package.AmmoSyncPacket;
import com.xapc.net.Package.AnimTriggerPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static java.lang.IO.print;
import static java.lang.IO.println;

public abstract class WeaponsAbstractClass extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final java.util.Map<java.util.UUID, Integer> reloadTicksMap = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Integer> ammoMap = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Integer> shootTicksMap = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Integer> reloadDelayMap = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Boolean> hadWeaponLastTick = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Integer> equipTicksMap = new java.util.HashMap<>();

    public static final RawAnimation EQUIP = RawAnimation.begin().thenPlay("equip");
    public static final RawAnimation IDLE_FPS = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation IDLE_3RD = RawAnimation.begin().thenLoop("idle_3rd");
    public static final RawAnimation SHOOT = RawAnimation.begin()
            .thenPlay("shoot");
    public static final RawAnimation RELOAD = RawAnimation.begin().thenPlay("reload");
    public static final RawAnimation RELOAD_3RD = RawAnimation.begin().thenPlay("reload_3rd");
    public static final RawAnimation RUN_FPS = RawAnimation.begin().thenLoop("run");

    public WeaponsAbstractClass(Properties properties) {
        super(properties);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static int getAmmo(java.util.UUID uuid, int maxAmmo) {
        return ammoMap.getOrDefault(uuid, maxAmmo);
    }

    public static void setAmmo(java.util.UUID uuid, int ammo) {
        ammoMap.put(uuid, ammo);
    }

    public static void triggerAnimForPlayer(ServerPlayer player, long instanceId, String controller, String anim) {
        ServerPlayNetworking.send(player, new AnimTriggerPacket(instanceId, controller, anim, false));
    }

    public static void stopAnimForPlayer(ServerPlayer player, long instanceId, String controller, String anim) {
        ServerPlayNetworking.send(player, new AnimTriggerPacket(instanceId, controller, anim, true));
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
        controllers.add(new AnimationController<>("base_controller", 3, state -> {

            final ItemDisplayContext context = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);

            // Только от первого лица — только наш локальный игрок
            if (!context.firstPerson()) {
                return PlayState.STOP;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return PlayState.STOP;

            if ((state.isCurrentAnimation(SHOOT) || state.isCurrentAnimation(RELOAD) || state.isCurrentAnimation(EQUIP))
                    && !state.controller().hasAnimationFinished()) {
                return PlayState.CONTINUE;
            }

            boolean isMoving = mc.player.getDeltaMovement().horizontalDistanceSqr() > 0.001;
            return state.setAndContinue(isMoving ? RUN_FPS : IDLE_FPS);
                }).receiveTriggeredAnimations()
                        .triggerableAnim("shoot", SHOOT)
                        .triggerableAnim("reload", RELOAD)
                        .triggerableAnim("reload_3rd", RELOAD_3RD)
                        .triggerableAnim("idle_3rd", IDLE_3RD)
                        .triggerableAnim("idle", IDLE_FPS)
                        .triggerableAnim("run", RUN_FPS)
                        .triggerableAnim("equip", EQUIP)
        );
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS; // по умолчанию ПКМ ничего не делает
    }

    // если ты не знаешь как это работает то даже не пытайся разобраться я сам хз
    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!level.isClientSide() && owner instanceof ServerPlayer player) {
            long animId = GeoItem.getOrAssignId(itemStack, level);
            java.util.UUID uuid = player.getUUID();

            // декремент счётчика выстрела
            int shootTicks = shootTicksMap.getOrDefault(uuid, 0);
            if (shootTicks > 0) {
                shootTicksMap.put(uuid, shootTicks - 1);
            }

            // декремент счётчика перезарядки
            int reloadTicks = reloadTicksMap.getOrDefault(uuid, 0);
            if (reloadTicks > 0) {
                reloadTicksMap.put(uuid, reloadTicks - 1); // <- декремент
                if (reloadTicks == 1) {
                    setAmmo(uuid, getMaxAmmo());
                    sendAmmoSync(player, itemStack, level, getMaxAmmo());
                }
            }
            sendAmmoSync(player, itemStack, level, getAmmo(uuid, getMaxAmmo()));

            if (slot == EquipmentSlot.MAINHAND) {
                boolean hadWeapon = hadWeaponLastTick.getOrDefault(uuid, false);
                int ammo = getAmmo(uuid, getMaxAmmo());
                boolean isShooting = shootTicksMap.getOrDefault(uuid, 0) > 0;
                boolean isReloading = reloadTicksMap.getOrDefault(uuid, 0) > 0;

                if (!hadWeapon) {
                    triggerAnimForPlayer(player, animId, "base_controller", "equip");
                    equipTicksMap.put(uuid, equipAnimationDurationTick());
                    reloadDelayMap.put(uuid, reloadDelay());
                }

                int equipTicks = equipTicksMap.getOrDefault(uuid, 0);
                if (equipTicks > 0) {
                    equipTicksMap.put(uuid, equipTicks - 1);
                }

                hadWeaponLastTick.put(uuid, true);

                if (isShooting) {
                    reloadDelayMap.put(uuid, reloadDelay());
                }

                int reloadDelay = reloadDelayMap.getOrDefault(uuid, 0);
                if (reloadDelay > 0) {
                    reloadDelayMap.put(uuid, reloadDelay - 1);
                }

                boolean delayPassed = reloadDelayMap.getOrDefault(uuid, 0) == 0;

                if (!isShooting && !isReloading && delayPassed && ammo < getMaxAmmo()) {
                    reloadTicksMap.put(uuid, reloadAnimationDurationTick());
                    triggerAnimForPlayer(player, animId, "base_controller", "reload");
                }
            } else {
                // Вот сюда — снаружи первого if
                hadWeaponLastTick.put(uuid, false);
            }
        }
        super.inventoryTick(itemStack, level, owner, slot);
    }

    private static void sendAmmoSync(ServerPlayer player, ItemStack stack, ServerLevel level, int ammo) {
        ServerPlayNetworking.send(player, new AmmoSyncPacket(player.getUUID(), ammo));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    public abstract int equipAnimationDurationTick();
    public abstract int reloadDelay();
    public abstract int getMaxAmmo();
    public abstract float getDamage();
    public abstract int shootAnimationDurationTick();
    public abstract int reloadAnimationDurationTick();

    public abstract net.minecraft.resources.Identifier getIdleAnimationId();
    public abstract net.minecraft.resources.Identifier getShootAnimationId();
    public abstract net.minecraft.resources.Identifier getReloadAnimationId();
}