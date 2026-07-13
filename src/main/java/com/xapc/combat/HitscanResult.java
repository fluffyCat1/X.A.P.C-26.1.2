package com.xapc.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record HitscanResult(Vec3 origin, Vec3 endPoint, Entity hitEntity, double distance) {
    public boolean hitSomething() {
        return hitEntity != null;
    }
}