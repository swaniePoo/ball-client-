package me.dinozoid.strife.module.implementations.player.hackerdetector.impl;

import me.dinozoid.strife.module.implementations.player.hackerdetector.Check;
import me.dinozoid.strife.util.hackerdetector.HackerDetectorUtil;
import net.minecraft.entity.EntityLivingBase;

public class SpeedA extends Check {
    public SpeedA() {
        super("Speed", "A", true);
    }

    @Override
    public void handleCheck(EntityLivingBase player) {
        double base = HackerDetectorUtil.getBaseMoveSpeed(player);
        double speed = Math.hypot(player.motionX, player.motionZ);
        if (speed > (base * 1.25f) && !HackerDetectorUtil.isOnGround(-0.1, player) && player.hurtTime == 0) {
            fail(player, String.format("S: %.2f, M: %.2f", speed, base));
        }
    }
}
