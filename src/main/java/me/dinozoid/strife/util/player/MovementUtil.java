package me.dinozoid.strife.util.player;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.event.implementations.player.MovePlayerEvent;
import me.dinozoid.strife.module.implementations.combat.KillAuraModule;
import me.dinozoid.strife.module.implementations.combat.TargetStrafeModule;
import me.dinozoid.strife.module.implementations.movement.SprintModule;
import me.dinozoid.strife.module.implementations.player.NoSlowdownModule;
import me.dinozoid.strife.module.implementations.player.ScaffoldModule;
import me.dinozoid.strife.util.MinecraftUtil;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.system.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockHopper;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.security.SecureRandom;

public class MovementUtil extends MinecraftUtil {

    public static void damage() {
        final double offset = 0.0624F;
        final NetHandlerPlayClient netHandler = mc.getNetHandler();
        final EntityPlayerSP player = mc.thePlayer;
        final double x = player.posX;
        final double y = player.posY;
        final double z = player.posZ;
        for (int i = 0; i < getMaxFallDist() / offset + 1; i++) {
            netHandler.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y + offset, z, false));
            netHandler.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.0001, z, false));

        }
        netHandler.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));
    }

    public static double getLastDist(EntityLivingBase entityLivingBase) {
        return MathUtil.getDistance(entityLivingBase.prevPosX, entityLivingBase.prevPosZ, entityLivingBase.posX, entityLivingBase.posZ);
    }

    public static double getRandomHypixelValues() {
        SecureRandom secureRandom = new SecureRandom();
        double value = secureRandom.nextDouble() * (1.0 / System.currentTimeMillis());
        for (int i = 0; i < MathUtil.randomInt(MathUtil.randomInt(4, 6), MathUtil.randomInt(8, 20)); i++)
            value *= (1.0 / System.currentTimeMillis());
        return value;
    }

    public static float getRandomHypixelValuesFloat() {
        SecureRandom secureRandom = new SecureRandom();
        float value = secureRandom.nextFloat() * (1f / System.currentTimeMillis());
        for (int i = 0; i < MathUtil.randomInt(MathUtil.randomInt(4, 6), MathUtil.randomInt(8, 20)); i++)
            value *= (1.0f / System.currentTimeMillis());
        return value;
    }

    public static boolean canSprint() {
        return (mc.thePlayer.movementInput.moveForward >= 0.8F || ((Boolean) SprintModule.propertyRepository().propertyBy(SprintModule.class, "Omni").getValue() && isMoving())) &&
                !mc.thePlayer.isCollidedHorizontally &&
                (mc.thePlayer.getFoodStats().getFoodLevel() > 6 ||
                        mc.thePlayer.capabilities.allowFlying) && ((ScaffoldModule.getInstance().toggled() && !ScaffoldModule.getInstance().noSprintProperty.getValue()) || !ScaffoldModule.getInstance().toggled()) &&
                !mc.thePlayer.isSneaking() &&
                (!mc.thePlayer.isUsingItem() || NoSlowdownModule.instance().toggled() || (Boolean) KillAuraModule.propertyRepository().propertyBy(KillAuraModule.class, "Keep Sprint").getValue()) &&
                !mc.thePlayer.isPotionActive(Potion.moveSlowdown.id);
    }

    public static boolean isMoving() {
        return mc.thePlayer.movementInput.moveForward != 0.0F || mc.thePlayer.movementInput.moveStrafe != 0.0F;
    }

    public static boolean isOnGround() {
        return mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically;
    }

    public static boolean isMovingOnGround() {
        return isMoving() && isOnGround();
    }

    public static boolean isOnGround(double height) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, -height, 0)).isEmpty();
    }

    public static void sendPositionAll(double value, boolean ground) {
        PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + value, mc.thePlayer.posY, mc.thePlayer.posZ + value, ground));
    }

    public static void sendPositionOnlyY(double y, boolean ground) {
        PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + y, mc.thePlayer.posZ, ground));
    }

    public static float getMovementDirection() {
        return getMovementDirection(mc.thePlayer.rotationYaw);
    }

    public static float getMovementDirection(final float yaw) {
        final float forward = mc.thePlayer.moveForward;
        final float strafe = mc.thePlayer.moveStrafing;
        final boolean forwards = forward > 0;
        final boolean backwards = forward < 0;
        final boolean right = strafe > 0;
        final boolean left = strafe < 0;
        float direction = 0;
        if(backwards)
            direction += 180;
        direction += forwards ? (right ? -45 : left ? 45 : 0) : backwards ? (right ? 45 : left ? -45 : 0) : (right ? -90 : left ? 90 : 0);
        direction += yaw;
        return MathHelper.wrapAngleTo180_float(direction);
    }

    public static double getBaseMoveSpeed() {
        return getBaseMoveSpeed(true);
    }

    public static double[] yawPos(double value) {
        return yawPos(mc.thePlayer.rotationYaw * MathHelper.deg2Rad, value);
    }

    public static double[] yawPos(float yaw, double value) {
        return new double[]{-MathHelper.sin(yaw) * value, MathHelper.cos(yaw) * value};
    }

    public static double getBaseMoveSpeed(boolean sprint) {
        double baseSpeed = (canSprint() || sprint) ? 0.2873 : 0.22;
        if ((mc.thePlayer != null && mc.thePlayer.isPotionActive(Potion.moveSpeed)) && sprint) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

    public static double getJumpHeight(double height) {
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
            return height + (amplifier + 1) * 0.1F;
        }
        return height;
    }

    public static float getMaxFallDist() {
        PotionEffect jump = mc.thePlayer.getActivePotionEffect(Potion.jump);
        final int height = jump != null ? jump.getAmplifier() + 1 : 0;
        return (float) (mc.thePlayer.getMaxFallHeight() + height);
    }

    public static void setSpeed(final MovePlayerEvent event, double speed) {
        EntityPlayerSP player = mc.thePlayer;
        TargetStrafeModule targetStrafeModule = Client.INSTANCE.getModuleRepository().moduleBy(TargetStrafeModule.class);
        if (targetStrafeModule.shouldStrafe()) {
            if (targetStrafeModule.shouldAdaptSpeed())
                speed = Math.min(speed, targetStrafeModule.getAdaptedSpeed());
            targetStrafeModule.setSpeed(event, speed);
            return;
        }

        setSpeed(event, speed, player.moveForward, player.moveStrafing, player.rotationYaw);
    }

    public static void setSpeed(MovePlayerEvent e, double speed, float forward, float strafing, float yaw) {
        if (forward == 0.0F && strafing == 0.0F) return;

        boolean reversed = forward < 0.0f;
        float strafingYaw = 90.0f *
                (forward > 0.0f ? 0.5f : reversed ? -0.5f : 1.0f);

        if (reversed)
            yaw += 180.0f;
        if (strafing > 0.0f)
            yaw -= strafingYaw;
        else if (strafing < 0.0f)
            yaw += strafingYaw;

        double x = StrictMath.cos(StrictMath.toRadians(yaw + 90.0f));
        double z = StrictMath.cos(StrictMath.toRadians(yaw));

        e.setX(x * speed);
        e.setZ(z * speed);
    }

    public static void setSpeed(double speed) {
        EntityPlayerSP player = mc.thePlayer;
        TargetStrafeModule targetStrafeModule = Client.INSTANCE.getModuleRepository().moduleBy(TargetStrafeModule.class);
        if (targetStrafeModule.shouldStrafe()) {
            if (targetStrafeModule.shouldAdaptSpeed())
                speed = Math.min(speed, targetStrafeModule.getAdaptedSpeed());
            MovementUtil.setSpeed(speed, 1, 0,
                    PlayerUtil.calculateYawFromSrcToDst(player.rotationYaw,
                            player.posX, player.posZ,
                            targetStrafeModule.currentPoint.point.xCoord, targetStrafeModule.currentPoint.point.zCoord));
            return;
        }

        setSpeed(speed, player.moveForward, player.moveStrafing, player.rotationYaw);
    }

    public static void setSpeed(double speed, float forward, float strafing, float yaw) {
        if (forward == 0.0F && strafing == 0.0F) return;

        boolean reversed = forward < 0.0f;
        float strafingYaw = 90.0f *
                (forward > 0.0f ? 0.5f : reversed ? -0.5f : 1.0f);

        if (reversed)
            yaw += 180.0f;
        if (strafing > 0.0f)
            yaw -= strafingYaw;
        else if (strafing < 0.0f)
            yaw += strafingYaw;

        double x = StrictMath.cos(StrictMath.toRadians(yaw + 90.0f));
        double z = StrictMath.cos(StrictMath.toRadians(yaw));

        mc.thePlayer.motionX = x * speed;
        mc.thePlayer.motionZ = z * speed;
    }

    public static double[] getSpeed(double moveSpeed) {
        final double forward = mc.thePlayer.movementInput.moveForward;
        final double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if (forward == 0 && strafe == 0) return new double[]{0, 0};
        final boolean reversed = forward < 0f;
        final float strafingYaw = 90f * (forward > 0f ? 0.5f : reversed ? -0.5f : 1.0f);
        if (reversed) yaw += 180f;
        if (strafe > 0f) yaw -= strafingYaw;
        else if (strafe < 0f) yaw += strafingYaw;
        final double x = Math.cos(StrictMath.toRadians(yaw + 90f));
        final double z = Math.cos(StrictMath.toRadians(yaw));
        return new double[]{x * moveSpeed, z * moveSpeed};
    }

    public static boolean isInsideBlock() {
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int y = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY); y < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxY) + 1; y++) {
                for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                    Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != null && !(block instanceof BlockAir)) {
                        AxisAlignedBB boundingBox = block.getCollisionBoundingBox(mc.theWorld, new BlockPos(x, y, z), mc.theWorld.getBlockState(new BlockPos(x, y, z)));
                        if (block instanceof BlockHopper)
                            boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
                        if (boundingBox != null && mc.thePlayer.getEntityBoundingBox().intersectsWith(boundingBox))
                            return true;
                    }
                }
            }
        }
        return false;
    }

}
