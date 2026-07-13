package com.xapc;

import com.xapc.registry.ItemRegistry;
import com.xapc.net.NetWorking;
import com.xapc.net.Package.TracerBeamPacket;
import com.xapc.registry.ModEntities;
import com.xapc.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Xapc implements ModInitializer {
    public static final String MOD_ID = "xapc";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
        ItemRegistry.initialize();
        NetWorking.register();
        ModSounds.init();
        TracerBeamPacket.register();
        ModEntities.init();
    }
}