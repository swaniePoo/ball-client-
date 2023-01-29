package me.dinozoid.strife.util.player;

import me.dinozoid.strife.module.implementations.player.ScaffoldModule;
import me.dinozoid.strife.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

public class RotationUtil extends MinecraftUtil {

    private static final double RAD_TO_DEG = 180.0 / Math.PI;
    private static final double DEG_TO_RAD = Math.PI / 180.0;

    public static float getYawDifference(float a, float b) {
        return MathHelper.wrapAngleTo180_float(a - b);
    }

    public static MovingObjectPosition rayTraceBlocks(final Minecraft mc,
                                                      final Vec3 src,
                                                      final double reach,
                                                      final float yaw,
                                                      final float pitch) {
        return mc.theWorld.rayTraceBlocks(src,
                getDstVec(src, yaw, pitch, reach),
                false,
                false,
                true);
    }

    public static Vec3 getDstVec(final Vec3 src,
                                 final float yaw,
                                 final float pitch,
                                 final double reach) {
        final Vec3 rotationVec = getPointedVec(yaw, pitch);
        return src.addVector(rotationVec.xCoord * reach,
                rotationVec.yCoord * reach,
                rotationVec.zCoord * reach);
    }


    public static Vec3 getPointedVec(final float yaw,
                                     final float pitch) {
        final double theta = -Math.cos(-pitch * DEG_TO_RAD);

        return new Vec3(Math.sin(-yaw * DEG_TO_RAD - Math.PI) * theta,
                Math.sin(-pitch * DEG_TO_RAD),
                Math.cos(-yaw * DEG_TO_RAD - Math.PI) * theta);
    }

    public static MovingObjectPosition rayTraceBlocks(final Minecraft mc,
                                                      final float yaw,
                                                      final float pitch) {
        return rayTraceBlocks(mc, getHitOrigin(mc.thePlayer), mc.playerController.getBlockReachDistance(), yaw, pitch);
    }




    public static float[] getRotationFromPosition(double x, double y, double z) {
        double xDiff = x - mc.thePlayer.posX;
        double yDiff = y - mc.thePlayer.posY;
        double zDiff = z - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(yDiff, dist));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotations(final Vec3 start,
                                       final Vec3 dst) {
        final double xDif = dst.xCoord - start.xCoord;
        final double yDif = dst.yCoord - start.yCoord;
        final double zDif = dst.zCoord - start.zCoord;

        final double distXZ = Math.sqrt(xDif * xDif + zDif * zDif);

        return new float[]{
                (float) (Math.atan2(zDif, xDif) * RAD_TO_DEG) - 90.0F,
                (float) (-(Math.atan2(yDif, distXZ) * RAD_TO_DEG))
        };
    }

    public static float[] getRotations(final float[] lastRotations,
                                       final float smoothing,
                                       final Vec3 start,
                                       final Vec3 dst) {
        // Get rotations from start - dst
        final float[] rotations = getRotations(start, dst);
        // Apply smoothing to them
        applySmoothing(lastRotations, smoothing, rotations);
        return rotations;
    }

    public static Vec3 getHitOrigin(final Entity entity) {
        return new Vec3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
    }

    public static void applySmoothing(final float[] lastRotations,
                                      final float smoothing,
                                      final float[] dstRotation) {
        if (smoothing > 0.0F) {
            final float yawChange = MathHelper.wrapAngleTo180_float(dstRotation[0] - lastRotations[0]);
            final float pitchChange = MathHelper.wrapAngleTo180_float(dstRotation[1] - lastRotations[1]);

            final float smoothingFactor = Math.max(1.0F, smoothing / 10.0F);

            dstRotation[0] = lastRotations[0] + yawChange / smoothingFactor;
            dstRotation[1] = Math.max(Math.min(112, lastRotations[1] + pitchChange / smoothingFactor), -90.0F);
        }
    }


    public static float[] getRotationFromEntity(Entity entity) {
        return getRotationFromPosition(entity.posX, (entity.posY - mc.thePlayer.getEyeHeight()), entity.posZ);
    }

    public static float[] getRotationFromVector(Vec3 vector) {
        return getRotationFromPosition(vector.xCoord, vector.yCoord, vector.zCoord);
    }

    public static float[] getScaffoldRotations(BlockPos pos, EnumFacing face) {
        BlockPos newPos = pos.add(0.5, 0.5, 0.5)
                .add(MovementUtil.getRandomHypixelValues(),
                        MovementUtil.getRandomHypixelValues(),
                        MovementUtil.getRandomHypixelValues())
                .add(0, Math.min(90, -mc.thePlayer.getEyeHeight() - 25 + MovementUtil.getRandomHypixelValues()), 0)
                .offset(face.rotateAround(EnumFacing.Axis.Y).rotateAround(EnumFacing.Axis.Y), 1.25f);
        return getRotationFromPosition(newPos.getX(), newPos.getY(), newPos.getZ());
    }

    public static Vec3 getVectorForRotation(float yaw, float pitch) {
        float f = MathHelper.cos(-yaw * MathHelper.deg2Rad - MathHelper.PI);
        float f1 = MathHelper.sin(-yaw * MathHelper.deg2Rad - MathHelper.PI);
        float f2 = -MathHelper.cos(-pitch * MathHelper.deg2Rad);
        float f3 = MathHelper.sin(-pitch * MathHelper.deg2Rad);
        return new Vec3(f1 * f2, f3, f * f2);
    }

}
