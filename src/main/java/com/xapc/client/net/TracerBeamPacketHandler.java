package com.xapc.client.net;

import com.xapc.client.render.ActiveTracers;
import com.xapc.net.Package.TracerBeamPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.phys.Vec3;

public final class TracerBeamPacketHandler {

    private TracerBeamPacketHandler() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(TracerBeamPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Vec3 start = new Vec3(payload.startX(), payload.startY(), payload.startZ());
                Vec3 end = new Vec3(payload.endX(), payload.endY(), payload.endZ());

                // DEBUG
                var player = context.client().player;
                System.out.println("[TRACER DEBUG] clientPlayerPos=" + (player != null ? player.position() : "null")
                        + " received start=" + start + " end=" + end);

                ActiveTracers.add(start, end);
            });
        });
    }
}