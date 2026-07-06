package com.xapc.client;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.event.MolangEvent;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;

import java.util.HashMap;
import java.util.Map;

public final class ClientAnimationSetup {

    public static final Identifier BASE_LAYER_ID = Identifier.fromNamespaceAndPath("xapc", "animation");
    private static final Identifier IDLE_3RD_PLAYER_ID = Identifier.fromNamespaceAndPath("xapc", "idle");

    private static RawAnimation cachedIdle3rd;
    private static final Map<Identifier, RawAnimation> CHAINED_ANIM_CACHE = new HashMap<>();

    private ClientAnimationSetup() {}

    public static void register() {
        registerAnimationLayer();
        registerMolangQueries();
    }

    private static void registerAnimationLayer() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(BASE_LAYER_ID, 1100,
                player -> new PlayerAnimationController(player,
                        (controller, state, animSetter) -> {
                            if (!controller.isPlayingTriggeredAnimation()) {
                                RawAnimation idle = getIdle3rdAnimation();
                                if (idle != null) {
                                    return animSetter.setAnimation(idle);
                                }
                            }
                            return PlayState.STOP;
                        }
                )
        );
    }

    private static void registerMolangQueries() {
        MolangEvent.MOLANG_EVENT.register((controller, engine, queryBinding) -> {
            if (controller instanceof PlayerAnimationController playerController) {
                MolangLoader.setDoubleQuery(queryBinding, "aim_pitch", ctrl -> {
                    Avatar avatar = playerController.getAvatar();
                    return avatar.getViewXRot(1.0F);
                });
            }
        });
    }

    static RawAnimation getIdle3rdAnimation() {
        if (cachedIdle3rd == null && PlayerAnimResources.hasAnimation(IDLE_3RD_PLAYER_ID)) {
            cachedIdle3rd = RawAnimation.begin()
                    .then(PlayerAnimResources.getAnimation(IDLE_3RD_PLAYER_ID), Animation.LoopType.LOOP);
        }
        return cachedIdle3rd;
    }

    public static RawAnimation getChainedWithIdle(Identifier actionId, Animation actionAnim) {
        return CHAINED_ANIM_CACHE.computeIfAbsent(actionId, id -> {
            RawAnimation chain = RawAnimation.begin().then(actionAnim, Animation.LoopType.PLAY_ONCE);
            Animation idleAnim = PlayerAnimResources.getAnimation(IDLE_3RD_PLAYER_ID);
            if (idleAnim != null) {
                chain = chain.thenLoop(idleAnim);
            }
            return chain;
        });
    }
}