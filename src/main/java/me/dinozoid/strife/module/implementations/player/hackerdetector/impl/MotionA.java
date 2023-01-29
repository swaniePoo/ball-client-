package me.dinozoid.strife.module.implementations.player.hackerdetector.impl;

import me.dinozoid.strife.module.implementations.player.hackerdetector.Check;
import net.minecraft.entity.EntityLivingBase;

public class MotionA extends Check {
    private double lastMotionY;
    private double motionY;
    public MotionA() {
        super("Motion", "A", false);
    }

    @Override
    public void handleCheck(EntityLivingBase player) {
        this.lastMotionY = motionY;
        this.motionY = player.motionY;
        double deltaY = motionY - lastMotionY;
        if (!player.onGround && motionY == 0.0D && lastMotionY == 0.0D && ++buffer > 5) {
//            fail(player, "deltaY=" + deltaY);
        }

    }
}
