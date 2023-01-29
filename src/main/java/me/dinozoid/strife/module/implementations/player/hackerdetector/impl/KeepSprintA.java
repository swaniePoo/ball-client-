package me.dinozoid.strife.module.implementations.player.hackerdetector.impl;

import me.dinozoid.strife.module.implementations.player.hackerdetector.Check;
import net.minecraft.entity.EntityLivingBase;

public class KeepSprintA extends Check {
    private boolean sprinting;
    private boolean wasWasSprinting;
    private boolean wasSprinting;
    private float fallDist;
    private float lastLastFallDist;
    private float lastFallDist;
    public KeepSprintA() {
        super("KeepSprint", "A", true);
    }

    @Override
    public void handleCheck(EntityLivingBase player) {
//        this.lastLastFallDist = lastFallDist;
//        this.lastFallDist = fallDist;
//        this.wasWasSprinting = wasSprinting;
//        this.wasSprinting = sprinting;
//        sprinting = player.isSprinting();
//        fallDist = player.fallDistance;
//        if (player.hurtTime > 0 && sprinting && wasSprinting && wasWasSprinting && fallDist < 0.6f && lastFallDist < 0.6f && lastLastFallDist < 0.6f) {
//            fail(player, "");
//        }
    }
}
