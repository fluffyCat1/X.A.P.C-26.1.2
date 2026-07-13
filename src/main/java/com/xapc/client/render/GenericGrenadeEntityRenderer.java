package com.xapc.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.xapc.entity.GenericGrenadeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class GenericGrenadeEntityRenderer extends GeoEntityRenderer<GenericGrenadeEntity, EntityRenderState> {
    public GenericGrenadeEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new GenericGrenadeGeoModel());
        this.shadowRadius = 0.15F;
    }
}