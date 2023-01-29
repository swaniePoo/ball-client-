package me.dinozoid.strife.util.hackerdetector;

import me.dinozoid.strife.util.MinecraftUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;

public class HackerDetectorUtil extends MinecraftUtil {
    public static double getBaseMoveSpeed(EntityLivingBase player) {
        double baseSpeed = 0.2873;
        if (player != null && player.isPotionActive(Potion.moveSpeed)) {
            int amplifier = player.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

    public static boolean isMoving(EntityLivingBase player) {
        return player.moveForward != 0.0F || player.moveStrafing != 0.0F;
    }

    public static boolean isOnGround(EntityLivingBase player) {
        return player.onGround && player.isCollidedVertically;
    }

    public static boolean isMovingOnGround(EntityLivingBase player) {
        return isMoving(player) && isOnGround(player);
    }

    public static boolean isOnGround(double height, EntityLivingBase player) {
        return !mc.theWorld.getCollidingBoundingBoxes(player, player.getEntityBoundingBox().offset(0, -height, 0)).isEmpty();
    }
}
