package com.xapc.utils;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.xapc.Xapc;
import com.xapc.client.render.GenericGrenadeRenderer;
import com.xapc.net.Package.AnimTriggerPacket;
import com.xapc.net.Package.GrenadeSyncPacket;
import com.xapc.net.Package.PlayerAnimBroadcastPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class GrenadesAbstractClass extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ----- состояние по (игрок+стак) -----
    public static final java.util.Map<WeaponKey, Integer> grenadeCountMap = new java.util.HashMap<>(); // текущий запас гранат
    public static final java.util.Map<WeaponKey, Integer> pendingThrowMap = new java.util.HashMap<>(); // тики до автоброска после раннего отпускания
    public static final java.util.Map<WeaponKey, Integer> regenTicksMap = new java.util.HashMap<>();   // накопленные тики к следующему пополнению
    public static final java.util.Map<WeaponKey, Integer> launchLockMap = new java.util.HashMap<>();   // блокировка на время анимации launch
    public static final java.util.Map<java.util.UUID, Boolean> hadGrenadeLastTick = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Integer> equipTicksMap = new java.util.HashMap<>();
    public static final java.util.Map<WeaponKey, Integer> throwCooldownMap = new java.util.HashMap<>();

    // ----- тайминги закукивания (в тиках, 20 тиков = 1 секунда) -----
    public static final int PRELAUNCH_MIN_TICKS = 15;   // 0.75с — чека срывается, раньше отпустить/отменить нельзя
    public static final int PRELAUNCH_WINDOW_TICKS = 20; // 1с — окно, в котором регулируется длина фитиля
    public static final int USE_DURATION_TICKS = PRELAUNCH_MIN_TICKS + PRELAUNCH_WINDOW_TICKS; // 35 тиков = 1.75с
    protected abstract void spawnGrenadeEntity(ServerPlayer player, ItemStack stack, int fuseTicks, float power);

    public abstract int softTossFuseTicks();              // фитиль для подброса под себя
    public abstract int softTossAnimationDurationTick();   // блокировка на время анимации подброса

    // ----- анимации -----
    public static final RawAnimation EQUIP = RawAnimation.begin().thenPlay("equip");
    public static final RawAnimation IDLE_FPS = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation IDLE_3RD = RawAnimation.begin().thenLoop("idle_3rd");
    public static final RawAnimation PRELAUNCH = RawAnimation.begin().thenPlay("pre-launch"); // единый клип на 1.75с
    public static final RawAnimation PRELAUNCH_3RD = RawAnimation.begin().thenPlay("pre_launch_3rd");
    public static final RawAnimation LAUNCH = RawAnimation.begin().thenPlay("launch");
    public static final RawAnimation SOFT_LAUNCH = RawAnimation.begin().thenPlay("soft-laucnh");
    public static final RawAnimation LAUNCH_3RD = RawAnimation.begin().thenPlay("launch_3rd");

    public abstract SoundEvent getPinPullSound();
    public abstract SoundEvent getLaunchSound();

    public float getLaunchVolume() { return 0.8F; }
    public float getLaunchPitch()  { return 1F; }

    public abstract int getMaxGrenades();          // максимум в запасе, напр. 4
    public abstract int getRegenIntervalTicks();   // сколько тиков на восстановление 1 гранаты
    public abstract int baseFuseTicks();           // фитиль при отпускании ровно на 0.75с (без докукивания)
    public abstract int minFuseTicks();            // фитиль при полном закукивании (1.75с)
    public abstract int equipAnimationDurationTick();
    public abstract int launchAnimationDurationTick();
    public abstract int getThrowCooldownTicks(); // сколько тиков ждать между бросками

    public abstract Identifier getEquipAnimationId();
    public abstract Identifier getIdleAnimationId();
    public abstract Identifier getLaunchAnimationId();

    public GrenadesAbstractClass(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // ================= утилиты =================
    private static final float SOFT_TOSS_POWER = 0.35F; // подберите под ощущение "уронить под ноги"

    private void doSoftToss(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        WeaponKey key = WeaponKey.of(player, stack);

        if (getGrenadeCount(key, getMaxGrenades()) <= 0) return;
        if (launchLockMap.getOrDefault(key, 0) > 0) return;
        if (equipTicksMap.getOrDefault(player.getUUID(), 0) > 0) return; // новая проверка
        if (pendingThrowMap.containsKey(key)) return;
        if (player.isUsingItem()) return;

        int count = getGrenadeCount(key, getMaxGrenades());
        setGrenadeCount(key, count - 1);
        sendGrenadeSync(player, getGrenadeCount(key, getMaxGrenades()));

        spawnGrenadeEntity(player, stack, softTossFuseTicks(), SOFT_TOSS_POWER);
        playWeaponSound(player, getPinPullSound(), 0.5F, 1.15F); // тише и выше по тону, чем полноценный launch

        long animId = instanceIdFor(player.getUUID());
        triggerAnimForPlayer(player, animId, "base_controller", "soft-laucnh"); // переиспользуем launch-анимацию
        broadcastAnimTrigger(player, animId, "third_person_controller", "launch_3rd");

        launchLockMap.put(key, softTossAnimationDurationTick());
    }

    public static long instanceIdFor(UUID uuid) {
        return uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
    }

    public static int getGrenadeCount(WeaponKey key, int max) {
        return grenadeCountMap.getOrDefault(key, max);
    }

    public static void setGrenadeCount(WeaponKey key, int count) {
        grenadeCountMap.put(key, Math.max(0, count));
    }

    public static void playWeaponSound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        if (sound == null) return; // защита от NPE, если звук ещё не задан у конкретной гранаты
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, net.minecraft.sounds.SoundSource.PLAYERS, volume, pitch);
    }

    public static void triggerAnimForPlayer(ServerPlayer player, long instanceId, String controller, String anim) {
        ServerPlayNetworking.send(player, new AnimTriggerPacket(player.getUUID(), instanceId, controller, anim, false));
    }

    public static void stopAnimForPlayer(ServerPlayer player, long instanceId, String controller, String anim) {
        ServerPlayNetworking.send(player, new AnimTriggerPacket(player.getUUID(), instanceId, controller, anim, true));
    }

    public static void broadcastAnimTrigger(ServerPlayer player, long instanceId, String controller, String anim) {
        AnimTriggerPacket packet = new AnimTriggerPacket(player.getUUID(), instanceId, controller, anim, false);
        player.level().getServer().getPlayerList().getPlayers().forEach(p -> ServerPlayNetworking.send(p, packet));
    }

    public static void broadcastStopAnimTrigger(ServerPlayer player, long instanceId, String controller, String anim) {
        AnimTriggerPacket packet = new AnimTriggerPacket(player.getUUID(), instanceId, controller, anim, true);
        player.level().getServer().getPlayerList().getPlayers().forEach(p -> ServerPlayNetworking.send(p, packet));
    }

    public static void broadcastPlayerAnim(ServerPlayer player, Identifier animId) {
        PlayerAnimBroadcastPacket packet = new PlayerAnimBroadcastPacket(player.getUUID(), animId);
        player.level().getServer().getPlayerList().getPlayers().forEach(p -> ServerPlayNetworking.send(p, packet));
    }

    private static void sendGrenadeSync(ServerPlayer player, int count) {
        ServerPlayNetworking.send(player, new GrenadeSyncPacket(player.getUUID(), count));
    }

    // ================= рендер / геколиб =================

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private final Supplier<GenericGrenadeRenderer> renderer = Suppliers.memoize(() ->
                    new GenericGrenadeRenderer(GrenadesAbstractClass.this)
            );

            @Override
            @Nullable
            public GeoItemRenderer<GrenadesAbstractClass> getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("base_controller", 3, state -> {
                    final ItemDisplayContext context = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);

                    if (!context.firstPerson()) {
                        return PlayState.STOP;
                    }

            if ((state.isCurrentAnimation(PRELAUNCH)
                    || state.isCurrentAnimation(LAUNCH)
                    || state.isCurrentAnimation(SOFT_LAUNCH)  // добавить эту строку
                    || state.isCurrentAnimation(EQUIP))
                    && !state.controller().hasAnimationFinished()) {
                return PlayState.CONTINUE;
            }

                    return state.setAndContinue(IDLE_FPS);
                }).receiveTriggeredAnimations()
                        .triggerableAnim("equip", EQUIP)
                        .triggerableAnim("idle", IDLE_FPS)
                        .triggerableAnim("pre-launch", PRELAUNCH)
                        .triggerableAnim("soft-laucnh", SOFT_LAUNCH)
                        .triggerableAnim("launch", LAUNCH)
        );

        controllers.add(new AnimationController<>("third_person_controller", 3, state -> {
                    final ItemDisplayContext context = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);

                    if (context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                            && context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                        return PlayState.STOP;
                    }

                    if ((state.isCurrentAnimation(PRELAUNCH_3RD) || state.isCurrentAnimation(LAUNCH_3RD))
                            && !state.controller().hasAnimationFinished()) {
                        return PlayState.CONTINUE;
                    }

                    return state.setAndContinue(IDLE_3RD);
                }).receiveTriggeredAnimations()
                        .triggerableAnim("pre_launch_3rd", PRELAUNCH_3RD)
                        .triggerableAnim("launch_3rd", LAUNCH_3RD)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    // ================= бросок / закукивание =================

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        doSoftToss(serverPlayer, stack, hand);
        return InteractionResult.CONSUME;
    }

    public static void beginCook(ServerPlayer serverPlayer, ItemStack stack, InteractionHand hand, GrenadesAbstractClass grenade) {
        WeaponKey key = WeaponKey.of(serverPlayer, stack);

        if (getGrenadeCount(key, grenade.getMaxGrenades()) <= 0) return;
        if (launchLockMap.getOrDefault(key, 0) > 0) return;
        if (throwCooldownMap.getOrDefault(key, 0) > 0) return;
        if (equipTicksMap.getOrDefault(serverPlayer.getUUID(), 0) > 0) return; // новая проверка
        if (pendingThrowMap.containsKey(key)) return;
        if (serverPlayer.isUsingItem()) return;

        long animId = instanceIdFor(serverPlayer.getUUID());
        triggerAnimForPlayer(serverPlayer, animId, "base_controller", "pre-launch");
        broadcastAnimTrigger(serverPlayer, animId, "third_person_controller", "pre_launch_3rd");
        playWeaponSound(serverPlayer, grenade.getPinPullSound(), 0.8F, 1F);

        serverPlayer.startUsingItem(hand);
    }

    /**
     * Игрок отпустил ЛКМ до истечения getUseDuration().
     *
     * @return
     */
    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return false;

        WeaponKey key = WeaponKey.of(player, stack);
        int timeCharged = USE_DURATION_TICKS - timeLeft;

        Xapc.LOGGER.info("[grenade] releaseUsing: timeLeft={}, timeCharged={}", timeLeft, timeCharged);

        if (timeCharged < PRELAUNCH_MIN_TICKS) {
            int ticksLeftToMin = PRELAUNCH_MIN_TICKS - timeCharged;
            pendingThrowMap.put(key, ticksLeftToMin);
            return false;
        }

        int windowTicks = Math.min(timeCharged - PRELAUNCH_MIN_TICKS, PRELAUNCH_WINDOW_TICKS);
        doThrow(player, stack, windowTicks);
        return false;
    }

    /** Игрок продержал ЛКМ до конца getUseDuration() — форс-бросок с минимальным фитилём. */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            doThrow(player, stack, PRELAUNCH_WINDOW_TICKS);
        }
        return stack;
    }

    private void doThrow(ServerPlayer player, ItemStack stack, int windowTicks) {
        WeaponKey key = WeaponKey.of(player, stack);
        int count = getGrenadeCount(key, getMaxGrenades());
        if (count <= 0) return;

        float progress = windowTicks / (float) PRELAUNCH_WINDOW_TICKS;
        int fuseTicks = Math.round(baseFuseTicks() + (minFuseTicks() - baseFuseTicks()) * progress);

        setGrenadeCount(key, count - 1);
        sendGrenadeSync(player, getGrenadeCount(key, getMaxGrenades()));

        spawnGrenadeEntity(player, stack, fuseTicks, 1.4F); // прежняя полная сила, теперь явным параметром
        playWeaponSound(player, getLaunchSound(), getLaunchVolume(), getLaunchPitch());

        long animId = instanceIdFor(player.getUUID());
        Xapc.LOGGER.info("[grenade] doThrow: instanceId={}, sending stop(pre-launch) + trigger(launch)", animId);
        Xapc.LOGGER.info("[grenade] doThrow: instanceId={}, windowTicks={}, fuseTicks={}",
                animId, windowTicks, fuseTicks);

        stopAnimForPlayer(player, animId, "base_controller", "pre-launch");
        triggerAnimForPlayer(player, animId, "base_controller", "launch");

        broadcastAnimTrigger(player, animId, "third_person_controller", "launch_3rd");

        launchLockMap.put(key, launchAnimationDurationTick());
        throwCooldownMap.put(key, getThrowCooldownTicks());
    }

    // ================= тик: авто-восстановление запаса =================

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!level.isClientSide() && owner instanceof ServerPlayer player) {
            WeaponKey key = WeaponKey.of(player, itemStack);
            UUID uuid = player.getUUID();

            int count = getGrenadeCount(key, getMaxGrenades());
            if (count < getMaxGrenades()) {
                int regenTicks = regenTicksMap.getOrDefault(key, 0) + 1;
                if (regenTicks >= getRegenIntervalTicks()) {
                    setGrenadeCount(key, count + 1);
                    regenTicks = 0;
                }
                regenTicksMap.put(key, regenTicks);
            } else {
                regenTicksMap.put(key, 0);
            }

            int lock = launchLockMap.getOrDefault(key, 0);
            if (lock > 0) {
                int newLock = lock - 1;
                launchLockMap.put(key, newLock);

                if (newLock == 0) {
                    // launch/soft-launch анимация только что закончилась — проигрываем equip
                    long animId = instanceIdFor(uuid);
                    triggerAnimForPlayer(player, animId, "base_controller", "equip");
                    broadcastPlayerAnim(player, getEquipAnimationId());
                    equipTicksMap.put(uuid, equipAnimationDurationTick());
                }
            }

            int cooldown = throwCooldownMap.getOrDefault(key, 0);
            if (cooldown > 0) {
                throwCooldownMap.put(key, cooldown - 1);
            }

            if (slot == EquipmentSlot.MAINHAND) {
                Integer pendingTicks = pendingThrowMap.get(key);
                if (pendingTicks != null) {
                    int remaining = pendingTicks - 1;
                    if (remaining <= 0) {
                        pendingThrowMap.remove(key);
                        doThrow(player, itemStack, 0);
                    } else {
                        pendingThrowMap.put(key, remaining);
                    }
                }
            } else {
                // граната покинула главную руку — отменяем отложенный автобросок,
                // иначе она взорвётся/кинется прямо из инвентаря
                pendingThrowMap.remove(key);
            }

            if (slot == EquipmentSlot.MAINHAND) {
                boolean hadGrenade = hadGrenadeLastTick.getOrDefault(uuid, false);
                long animId = instanceIdFor(uuid);

                if (!hadGrenade) {
                    triggerAnimForPlayer(player, animId, "base_controller", "equip");
                    broadcastPlayerAnim(player, getEquipAnimationId());
                    equipTicksMap.put(uuid, equipAnimationDurationTick());
                }

                int equipTicks = equipTicksMap.getOrDefault(uuid, 0);
                if (equipTicks > 0) {
                    equipTicksMap.put(uuid, equipTicks - 1);
                }

                hadGrenadeLastTick.put(uuid, true);
                sendGrenadeSync(player, getGrenadeCount(key, getMaxGrenades()));
            } else {
                hadGrenadeLastTick.put(uuid, false);
            }
        }
        super.inventoryTick(itemStack, level, owner, slot);
    }
}