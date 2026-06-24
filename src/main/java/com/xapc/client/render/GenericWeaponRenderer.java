package com.xapc.client.render;

import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;

public class GenericWeaponRenderer extends GeoItemRenderer<WeaponsAbstractClass> {
    public GenericWeaponRenderer(WeaponsAbstractClass item) {
        super(item);

        withRenderLayer(skinHandLayer("right_hand"));
        withRenderLayer(skinHandLayer("left_hand"));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        ItemDisplayContext perspective = renderPassInfo.renderState()
                .getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.NONE);

        boolean isGui = perspective == ItemDisplayContext.GUI ||
                perspective == ItemDisplayContext.FIXED ||
                perspective == ItemDisplayContext.GROUND;

        boolean shouldShowArms = perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

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

    private CustomBoneTextureGeoLayer<WeaponsAbstractClass, GeoItemRenderer.RenderData, GeoRenderState> skinHandLayer(String boneName) {
        return new CustomBoneTextureGeoLayer<>(this, boneName, DefaultPlayerSkin.getDefaultSkin().body().texturePath()) {
            @Override
            protected Identifier getTextureResource(GeoRenderState renderState) {
                LocalPlayer player = Minecraft.getInstance().player;
                return player != null ? player.getSkin().body().texturePath() : super.getTextureResource(renderState);
            }

            @Override
            public boolean shouldRenderBone(GeoRenderState renderState) {
                ItemDisplayContext perspective = renderState.getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.NONE);
                return perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                        perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            }
        };
    }
}