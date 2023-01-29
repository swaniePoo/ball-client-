package me.dinozoid.strife.module.implementations.player.hackerdetector.impl;

import me.dinozoid.strife.module.implementations.player.hackerdetector.Check;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;

public class SpeedB extends Check {
    public SpeedB() {
        super("Speed", "B", true);
    }

    @Override
    public void handleCheck(EntityLivingBase player) {
        double deltaXZ = Math.hypot(player.motionX, player.motionZ);
        double max = (player.isPotionActive(Potion.moveSpeed) ? 0.331 + player.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0.331);
        if (deltaXZ > max && player.hurtTime == 0) {
            if (++buffer > 5) {
                fail(player, String.format("S: %.2f, M: %.2f", deltaXZ, max));
            }
        } else {
            buffer -= (buffer > 0) ? 0.5 : 0;
        }
    }
}
