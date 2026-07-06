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
import com.xapc.client.render.GenericWeaponRenderer;
import com.xapc.net.Package.AmmoSyncPacket;
import com.xapc.net.Package.AnimTriggerPacket;
import com.xapc.net.Package.PlayerAnimBroadcastPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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

import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.IO.println;

public abstract class WeaponsAbstractClass extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final java.util.Map<WeaponKey, Integer> reloadTicksMap = new java.util.HashMap<>();
    public static final java.util.Map<WeaponKey, Integer> ammoMap = new java.util.HashMap<>();
    public static final java.util.Map<WeaponKey, Integer> shootTicksMap = new java.util.HashMap<>();
    public static final java.util.Map<WeaponKey, Integer> reloadDelayMap = new java.util.HashMap<>();
    // hadWeaponLastTick и equipTicksMap оставляем как есть, по UUID
    public static final java.util.Map<java.util.UUID, Boolean> hadWeaponLastTick = new java.util.HashMap<>();
    public static final java.util.Map<java.util.UUID, Integer> equipTicksMap = new java.util.HashMap<>();

    public static final RawAnimation EQUIP = RawAnimation.begin().thenPlay("equip");
    public static final RawAnimation EQUIP_ALT = RawAnimation.begin().thenPlay("equip2"); // новая
    public static final RawAnimation IDLE_FPS = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation IDLE_3RD = RawAnimation.begin().thenLoop("idle_3rd");
    public static final RawAnimation SHOOT = RawAnimation.begin()
            .thenPlay("shoot");
    public static final RawAnimation SHOOT_3RD = RawAnimation.begin()
            .thenPlay("shoot_3rd");
    public static final RawAnimation RELOAD = RawAnimation.begin().thenPlay("reload");
    public static final RawAnimation RELOAD_3RD = RawAnimation.begin().thenPlay("reload_3rd");

    public abstract SoundEvent getShootSound();
    public abstract SoundEvent getReloadSound();
//    public abstract SoundEvent getEquipSound();

    public float getShootVolume()  { return 0.75F; }
    public float getShootPitch()   { return 1F; }
    public float getReloadVolume() { return 0.75F; }
    public float getReloadPitch()  { return 1F; }
//    public float getEquipVolume()  { return 1.0F; }
//    public float getEquipPitch()   { return 1.0F; }

    public WeaponsAbstractClass(Properties properties) {
        super(properties);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static void playWeaponSound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, net.minecraft.sounds.SoundSource.PLAYERS, volume, pitch);
    }

    public static long instanceIdFor(java.util.UUID uuid) {
        return uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
    }

    private static void tagOwner(ItemStack stack, java.util.UUID uuid) {
        var tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        tag.putLong("xapc_owner_msb", uuid.getMostSignificantBits());
        tag.putLong("xapc_owner_lsb", uuid.getLeastSignificantBits());
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
    }

    public static int getAmmo(WeaponKey key, int maxAmmo) {
        return ammoMap.getOrDefault(key, maxAmmo);
    }

    public static void setAmmo(WeaponKey key, int ammo) {
        ammoMap.put(key, ammo);
    }
    // ЛОКАЛЬНО — только самому игроку
    public static void triggerAnimForPlayer(ServerPlayer player, long instanceId, String controller, String anim) {
        ServerPlayNetworking.send(player, new AnimTriggerPacket(player.getUUID(), instanceId, controller, anim, false));
    }

    public static void stopAnimForPlayer(ServerPlayer player, long instanceId, String controller, String anim) {
        ServerPlayNetworking.send(player, new AnimTriggerPacket(player.getUUID(), instanceId, controller, anim, true));
    }

    // ВСЕМ — третье лицо
    public static void broadcastAnimTrigger(ServerPlayer player, long instanceId, String controller, String anim) {
        AnimTriggerPacket packet = new AnimTriggerPacket(player.getUUID(), instanceId, controller, anim, false);
        player.level().getServer().getPlayerList().getPlayers().forEach(p -> ServerPlayNetworking.send(p, packet));
    }

    public static void broadcastStopAnimTrigger(ServerPlayer player, long instanceId, String controller, String anim) {
        AnimTriggerPacket packet = new AnimTriggerPacket(player.getUUID(), instanceId, controller, anim, true);
        player.level().getServer().getPlayerList().getPlayers().forEach(p -> ServerPlayNetworking.send(p, packet));
    }

    protected String chooseEquipAnimKey() {
        return "equip";
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
//
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("base_controller", 1, state -> {

            final ItemDisplayContext context = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);

            // Только от первого лица — только наш локальный игрок
            if (!context.firstPerson()) {
                return PlayState.STOP;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return PlayState.STOP;

            if ((state.isCurrentAnimation(SHOOT)
                    || state.isCurrentAnimation(RELOAD)
                    || state.isCurrentAnimation(EQUIP)
                    || state.isCurrentAnimation(EQUIP_ALT)) // <-- добавили
                    && !state.controller().hasAnimationFinished()) {
                return PlayState.CONTINUE;
            }

            return state.setAndContinue(IDLE_FPS);
                }).receiveTriggeredAnimations()
                        .triggerableAnim("shoot", SHOOT)
                        .triggerableAnim("reload", RELOAD)
                        .triggerableAnim("idle", IDLE_FPS)
                        .triggerableAnim("equip", EQUIP)
                        .triggerableAnim("equip2", EQUIP_ALT)
        );
        controllers.add(new AnimationController<>("third_person_controller", 3, state -> {
                    final ItemDisplayContext context = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);

                    if (context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                            && context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                        return PlayState.STOP;
                    }

                    if ((state.isCurrentAnimation(SHOOT) || state.isCurrentAnimation(RELOAD_3RD) || state.isCurrentAnimation(EQUIP))
                            && !state.controller().hasAnimationFinished()) {
                        return PlayState.CONTINUE;
                    }

                    return state.setAndContinue(IDLE_3RD);
                }).receiveTriggeredAnimations()
                        .triggerableAnim("shoot_3rd", SHOOT_3RD)
                        .triggerableAnim("reload_3rd", RELOAD_3RD)
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
            UUID uuid = player.getUUID();
            WeaponKey key = WeaponKey.of(player, itemStack); // <-- новый составной ключ
            long animId = instanceIdFor(uuid);
            tagOwner(itemStack, uuid);

            if (slot == EquipmentSlot.MAINHAND) {
                int shootTicks = shootTicksMap.getOrDefault(key, 0);
                if (shootTicks > 0) {
                    shootTicksMap.put(key, shootTicks - 1);
                }

                int reloadTicks = reloadTicksMap.getOrDefault(key, 0);
                if (reloadTicks > 0) {
                    reloadTicksMap.put(key, reloadTicks - 1);
                    if (reloadTicks == 1) {
                        setAmmo(key, getMaxAmmo());
                        sendAmmoSync(player, itemStack, level, getMaxAmmo());
                    }
                }
                sendAmmoSync(player, itemStack, level, getAmmo(key, getMaxAmmo()));

                boolean hadWeapon = hadWeaponLastTick.getOrDefault(uuid, false);
                int ammo = getAmmo(key, getMaxAmmo());
                boolean isShooting = shootTicksMap.getOrDefault(key, 0) > 0;
                boolean isReloading = reloadTicksMap.getOrDefault(key, 0) > 0;

                if (!hadWeapon) {
                    String equipKey = chooseEquipAnimKey();
                    triggerAnimForPlayer(player, animId, "base_controller", equipKey);
                    broadcastAnimTrigger(player, animId, "third_person_controller", "equip");
                    broadcastPlayerAnim(player, getEquipAnimationId());
                    equipTicksMap.put(uuid, equipAnimationDurationTick());
                    reloadDelayMap.put(key, reloadDelay());
                }

                int equipTicks = equipTicksMap.getOrDefault(uuid, 0);
                if (equipTicks > 0) {
                    equipTicksMap.put(uuid, equipTicks - 1);
                }

                hadWeaponLastTick.put(uuid, true);

                if (isShooting) {
                    reloadDelayMap.put(key, reloadDelay());
                }

                int reloadDelay = reloadDelayMap.getOrDefault(key, 0);
                if (reloadDelay > 0) {
                    reloadDelayMap.put(key, reloadDelay - 1);
                }

                boolean delayPassed = reloadDelayMap.getOrDefault(key, 0) == 0;

                if (!isShooting && !isReloading && delayPassed && ammo < getMaxAmmo()) {
                    reloadTicksMap.put(key, reloadAnimationDurationTick());
                    triggerAnimForPlayer(player, animId, "base_controller", "reload");
                    broadcastAnimTrigger(player, animId, "third_person_controller", "reload_3rd");
                    broadcastPlayerAnim(player, getReloadAnimationId());

                    playWeaponSound(player, getReloadSound(), getReloadVolume(), getReloadPitch());
                }
            } else {
                if (reloadTicksMap.getOrDefault(key, 0) > 0) {
                    reloadTicksMap.put(key, 0);
                    stopAnimForPlayer(player, animId, "base_controller", "reload");
                    broadcastStopAnimTrigger(player, animId, "third_person_controller", "reload_3rd");
                }
                hadWeaponLastTick.put(uuid, false);
            }
        }
        super.inventoryTick(itemStack, level, owner, slot);
    }

    public static void broadcastPlayerAnim(ServerPlayer player, Identifier animId) {
        PlayerAnimBroadcastPacket packet = new PlayerAnimBroadcastPacket(player.getUUID(), animId);
        // Отправляем всем включая себя
        player.level().getServer().getPlayerList().getPlayers().forEach(p -> {
            ServerPlayNetworking.send(p, packet);
        });
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
    public abstract net.minecraft.resources.Identifier getEquipAnimationId();
    public abstract int reloadAnimationDurationTick();

    public abstract net.minecraft.resources.Identifier getIdleAnimationId();
    public abstract net.minecraft.resources.Identifier getShootAnimationId();
    public abstract net.minecraft.resources.Identifier getReloadAnimationId();
}