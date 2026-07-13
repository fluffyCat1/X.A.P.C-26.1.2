package com.xapc.client.render;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ActiveTracers {
    private static final List<Tracer> TRACERS = new ArrayList<>();
    public static final long LIFETIME_MS = 150;

    public record Tracer(Vec3 start, Vec3 end, long spawnTime) {}

    public static void add(Vec3 start, Vec3 end) {
        TRACERS.add(new Tracer(start, end, System.currentTimeMillis()));
    }

    public static List<Tracer> getActive() {
        long now = System.currentTimeMillis();
        TRACERS.removeIf(t -> now - t.spawnTime() > LIFETIME_MS);
        return TRACERS;
    }
}