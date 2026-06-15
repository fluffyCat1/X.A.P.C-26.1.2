package com.xapc.client.render;

import com.xapc.utils.WeaponsAbstractClass;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.constant.DataTickets;
import com.geckolib.animation.AnimationController;
import net.minecraft.world.item.ItemStack;

import static com.xapc.client.XapcClient.BASE_LAYER_ID;
import static net.minecraft.resources.Identifier.fromNamespaceAndPath;

public class GenericWeaponRenderer extends GeoItemRenderer<WeaponsAbstractClass> {
    public GenericWeaponRenderer(WeaponsAbstractClass item) {
        super(item);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        // 1. Проверяем перспективу камеры
        ItemDisplayContext perspective = renderPassInfo.getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, null);
        boolean shouldShowArms = (perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);

        snapshots.get("right_hand").ifPresent(snapshot -> {
            snapshot.setScaleX(shouldShowArms ? 1.0F : 0.0F);
            snapshot.setScaleY(shouldShowArms ? 1.0F : 0.0F);
            snapshot.setScaleZ(shouldShowArms ? 1.0F : 0.0F);
        });

        snapshots.get("left_hand").ifPresent(snapshot -> {
            snapshot.setScaleX(shouldShowArms ? 1.0F : 0.0F);
            snapshot.setScaleY(shouldShowArms ? 1.0F : 0.0F);
            snapshot.setScaleZ(shouldShowArms ? 1.0F : 0.0F);
        });
    }
}