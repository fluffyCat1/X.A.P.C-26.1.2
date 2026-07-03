package com.xapc.client.render;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;
import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class GenericWeaponRenderer extends GeoItemRenderer<WeaponsAbstractClass> {
    public GenericWeaponRenderer(WeaponsAbstractClass item) {
        super(item);
        withRenderLayer(skinHandLayer("right_hand"));
        withRenderLayer(skinHandLayer("left_hand"));
    }

    @Override
    public long getInstanceId(WeaponsAbstractClass animatable, RenderData renderData) {
        ItemStack stack = renderData.itemStack();
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        if (tag.contains("xapc_owner_msb") && tag.contains("xapc_owner_lsb")) {
            long msb = tag.getLong("xapc_owner_msb").orElse(0L);
            long lsb = tag.getLong("xapc_owner_lsb").orElse(0L);
            java.util.UUID ownerUuid = new java.util.UUID(msb, lsb);
            return WeaponsAbstractClass.instanceIdFor(ownerUuid);
        }

        return super.getInstanceId(animatable, renderData);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        ItemDisplayContext perspective = renderPassInfo.renderState()
                .getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.NONE);

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
                return player != null
                        ? player.getSkin().body().texturePath()
                        : super.getTextureResource(renderState);
            }

            @Override
            public boolean shouldRenderBone(GeoRenderState renderState) {
                ItemDisplayContext perspective = renderState
                        .getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.NONE);
                return perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                        perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            }
        };
    }
}