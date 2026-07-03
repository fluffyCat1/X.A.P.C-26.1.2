package com.xapc.client.render;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xapc.utils.WeaponsAbstractClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import com.geckolib.constant.DataTickets;

import java.util.function.BiConsumer;

public class SkinArmLayer extends GeoRenderLayer<WeaponsAbstractClass, GeoItemRenderer.RenderData, GeoRenderState> {
    private final String boneName;
    private final boolean isRightHand;

    public SkinArmLayer(GeoRenderer<WeaponsAbstractClass, GeoItemRenderer.RenderData, GeoRenderState> renderer,
                        String boneName, boolean isRightHand) {
        super(renderer);
        this.boneName = boneName;
        this.isRightHand = isRightHand;
    }

    private boolean shouldRender(GeoRenderState renderState) {
        ItemDisplayContext perspective = renderState
                .getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.NONE);
        return perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    @Override
    public void addPerBoneRender(RenderPassInfo<GeoRenderState> renderPassInfo,
                                 BiConsumer<GeoBone, PerBoneRender<GeoRenderState>> consumer) {
        boolean should = shouldRender(renderPassInfo.renderState());
        System.out.println("[SkinArmLayer] addPerBoneRender bone=" + boneName + " shouldRender=" + should);

        if (!should) return;

        renderPassInfo.model().getBone(this.boneName).ifPresentOrElse(bone -> {
            System.out.println("[SkinArmLayer] bone found: " + boneName);
            consumer.accept(bone, this::renderArm);
        }, () -> System.out.println("[SkinArmLayer] bone NOT FOUND: " + boneName));
    }

    private void renderArm(RenderPassInfo<GeoRenderState> renderPassInfo,
                           GeoBone bone, SubmitNodeCollector renderTasks) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        var skinTexture = player.getSkin().body().texturePath();
        int packedLight = renderPassInfo.packedLight();
        int packedOverlay = renderPassInfo.packedOverlay();

        boolean isSlim = player.getSkin().model() == PlayerModelType.SLIM;
        var modelLayer = isSlim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER;

        var modelSet = Minecraft.getInstance().getEntityModels();
        HumanoidModel<?> playerModel = new HumanoidModel<>(modelSet.bakeLayer(modelLayer));

        ModelPart arm = isRightHand ? playerModel.rightArm : playerModel.leftArm;
        RenderType renderType = RenderTypes.entitySolid(skinTexture);

        var poseStack = renderPassInfo.poseStack();

        System.out.println("[SkinArmLayer] pose translation: " + poseStack.last().pose().getTranslation(new org.joml.Vector3f()));

        poseStack.pushPose();

        renderTasks.submitModelPart(arm, poseStack, renderType, packedLight, packedOverlay, null);

        poseStack.popPose();
    }
}