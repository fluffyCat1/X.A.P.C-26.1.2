package com.xapc.client.render;

import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.world.item.ItemDisplayContext;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.constant.DataTickets;

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