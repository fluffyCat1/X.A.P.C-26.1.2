package com.xapc.client.render;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.xapc.entity.GenericGrenadeEntity;
import net.minecraft.resources.Identifier;

public class GenericGrenadeGeoModel extends GeoModel<GenericGrenadeEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("xapc", "geckolib/models/entity/grenade_entity.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("xapc", "textures/entity/grenade_entity.png");
    }

    @Override
    public Identifier getAnimationResource(GenericGrenadeEntity animatable) {
        return Identifier.fromNamespaceAndPath("xapc", "geckolib/animations/entity/grenade_entity.animation.json");
    }
}