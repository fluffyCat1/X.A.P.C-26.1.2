package com.xapc.client;

import com.xapc.client.net.ClientNetworkingInit;
import com.xapc.client.net.ReloadSoundPacketHandler;
import com.xapc.client.net.TracerBeamPacketHandler;
import com.xapc.client.render.GenericGrenadeEntityRenderer;
import com.xapc.client.render.TracerRenderer;
import com.xapc.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class XapcClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.GENERIC_GRENADE, GenericGrenadeEntityRenderer::new);
        ClientAnimationSetup.register();
        ClientTickHandler.register();
        ClientNetworkingInit.register();
        TracerBeamPacketHandler.register();
        TracerRenderer.register();
        ReloadSoundPacketHandler.register();
    }
}