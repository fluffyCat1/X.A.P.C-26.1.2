package com.xapc.client;

import com.xapc.client.net.ClientNetworkingInit;
import net.fabricmc.api.ClientModInitializer;

public class XapcClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientAnimationSetup.register();
        ClientTickHandler.register();
        ClientNetworkingInit.register();
    }
}