package me.dinozoid.strife.module.implementations.combat;

import com.sun.org.apache.xpath.internal.operations.Mod;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.MovePlayerEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.player.PlayerStrafeEvent;
import me.dinozoid.strife.event.implementations.render.Render3DEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.ColorProperty;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.MathUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

@ModuleInfo(name = "TargetStrafe", renderName = "Target Strafe", category = Category.MOVEMENT)
public class TargetStrafeModule extends Module {
    // Need to be holding space
    public final Property<Boolean> holdSpaceProperty = new Property<Boolean>("Hold Space", true);
    // Pattern mode
    private final EnumProperty<Mode> modeProperty = new EnumProperty<>("Mode", Mode.FOLLOW);
    // Radius & points
    private final DoubleProperty pointsProperty = new DoubleProperty("Points", 12, 1, 90, 1);
    private final DoubleProperty radiusProperty = new DoubleProperty("Radius", 2.0, 0.1, 4.0, 0.1);
    // Adaptive
    private final Property<Boolean> adaptiveSpeedProperty = new Property<Boolean>("Adapt Speed", true);
    //Direction Keys
    private final Property<Boolean> directionKeyProperty = new Property<Boolean>("Direction Keys", true);
    // Render settings
    private final EnumProperty<RenderMode> renderProperty = new EnumProperty<>("Render Mode", RenderMode.POINTS);
    private final Property<Boolean> polyGradientProperty = new Property<Boolean>("Poly Gradient", true, () -> this.renderProperty.getValue() == RenderMode.POLYGON);
    // Colours
    private final ColorProperty activePointColorProperty = new ColorProperty("Active", new Color(0x8000FF00),
            this::shouldRender);
    private final ColorProperty dormantPointColorProperty = new ColorProperty("Dormant", new Color(0x20FFFFFF),
            this::shouldRender);
    private final ColorProperty invalidPointColorProperty = new ColorProperty("Invalid", new Color(0x20FF0000),
            () -> this.renderProperty.getValue() == RenderMode.POINTS || (this.polyGradientProperty.getValue()));
    // Render width
    private final DoubleProperty widthProperty = new DoubleProperty("Width", 1.0F,
            0.5F, 5.0F, 0.5F,  () -> this.renderProperty.getValue() == RenderMode.POLYGON);

    private final List<Point> currentPoints = new ArrayList<>();
    public EntityLivingBase currentTarget;
    private int direction = 1;
    public Point currentPoint;

    private KillAuraModule aura;

    private boolean shouldRender() {
        return this.renderProperty.getValue() != RenderMode.OFF;
    }

    @EventHandler
    public final Listener<Render3DEvent> onRender3DEvent = new Listener<>(event -> {
        if (this.shouldRender() && this.currentTarget != null) {
            if(mc.thePlayer.ticksExisted <= 5) return;
            final float partialTicks = event.getPartialTicks();
            final int dormantColor = this.dormantPointColorProperty.getValue().getRGB();
            final int invalidColor = this.invalidPointColorProperty.getValue().getRGB();

            // Disable texture binding
            glDisable(GL_TEXTURE_2D);
            // Enable blending
            boolean restore = RenderUtil.glEnableBlend();
            // Disable depth testing (see through walls)
            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);

            Point lastPoint = null;

            switch (this.renderProperty.getValue()) {
                case POINTS:
                    for (Point point : this.currentPoints) {
                        final Vec3 pos = point.calculateInterpolatedPos(partialTicks);
                        final double x = pos.xCoord;
                        final double y = pos.yCoord;
                        final double z = pos.zCoord;

                        final double pointSize = 0.03;
                        AxisAlignedBB bb = new AxisAlignedBB(x, y, z,
                                x + pointSize, y + pointSize, z + pointSize);

                        if (lastPoint == null ||
                                lastPoint == this.currentPoint ||
                                point == this.currentPoint ||
                                lastPoint.valid != point.valid) {
                            int color;

                            if (this.currentPoint == point)
                                color = this.activePointColorProperty.getValue().getRGB();
                            else if (point.valid) color = dormantColor;
                            else color = invalidColor;
                            RenderUtil.color(color);
                        }

                        RenderUtil.glDrawBoundingBox(bb, 0, true);

                        lastPoint = point;
                    }
                    break;
                case POLYGON:
                    // Enable line anti-aliasing
                    glEnable(GL_LINE_SMOOTH);
                    glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                    // Set line width
                    glLineWidth(this.widthProperty.getValue().floatValue());
                    final boolean polyGradient = this.polyGradientProperty.getValue();

                    if (polyGradient) glShadeModel(GL_SMOOTH);
                    else
                        RenderUtil.color(this.shouldStrafe() ? this.activePointColorProperty.getValue().getRGB() : dormantColor);

                    glBegin(GL_LINE_LOOP);

                    for (final Point point : this.currentPoints) {
                        if (polyGradient) {
                            if (lastPoint == null ||
                                    lastPoint == currentPoint ||
                                    point == currentPoint ||
                                    lastPoint.valid != point.valid) {
                                int color;

                                if (currentPoint == point)
                                    color = activePointColorProperty.getValue().getRGB();
                                else if (point.valid) color = dormantColor;
                                else color = invalidColor;
                                RenderUtil.color(color);
                            }
                            lastPoint = point;
                        }

                        final Vec3 pos = point.calculateInterpolatedPos(partialTicks);
                        final double x = pos.xCoord;
                        final double y = pos.yCoord;
                        final double z = pos.zCoord;

                        glVertex3d(x, y, z);
                    }

                    glEnd();

                    if (polyGradient)
                        glShadeModel(GL_FLAT);

                    glDisable(GL_LINE_SMOOTH);
                    break;
            }

            // Disable line anti-aliasing
            glDisable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
            // Enable depth testing
            glEnable(GL_DEPTH_TEST);
            glDepthMask(true);
            // Restore blend
            RenderUtil.glRestoreBlend(restore);
            // Enable texture drawing
            glEnable(GL_TEXTURE_2D);
        }
    });

    @EventHandler
    public final Listener<PlayerMotionEvent> onUpdatePositionEvent = new Listener<>(event -> {
        if (event.isPre()) {
            if(mc.thePlayer.ticksExisted <= 5) return;
            this.currentTarget = this.aura.target();

            if (this.currentTarget != null) {
                if (this.directionKeyProperty.getValue()) {
                    if (mc.gameSettings.keyBindLeft.isPressed()) {
                        this.direction = 1;
                    }

                    if (mc.gameSettings.keyBindRight.isPressed()) {
                        this.direction = -1;
                    }
                }

                this.collectPoints(this.pointsProperty.getValue().intValue(), this.radiusProperty.getValue(), this.currentTarget);
                this.currentPoint = this.findOptimalPoint(this.currentTarget, this.currentPoints);
            } else {
                this.currentPoint = null;
            }
        }
    });

    private Point findOptimalPoint(EntityLivingBase target, List<Point> points) {
        switch (modeProperty.getValue()) {
            case BEHIND:
                float biggestDif = -1.0F;
                Point bestPoint = null;

                for (Point point : points) {
                    if (point.valid) {
                        final float yawChange = Math.abs(PlayerUtil.calculateYawFromSrcToDst(target.rotationYaw, target.posX, target.posZ,
                                point.point.xCoord, point.point.zCoord));
                        if (yawChange > biggestDif) {
                            biggestDif = yawChange;
                            bestPoint = point;
                        }
                    }
                }
                return bestPoint;
            case FOLLOW:
                return getClosestPoint(mc.thePlayer.posX, mc.thePlayer.posZ, points);
            default:
                final Point closest = getClosestPoint(mc.thePlayer.posX, mc.thePlayer.posZ, points);

                if (closest == null)
                    return null;

                final int pointsSize = points.size();

                if (pointsSize == 1)
                    return closest;

                final int closestIndex = points.indexOf(closest);

                Point nextPoint;

                int passes = 0;

                do {
                    if (passes > pointsSize) // Note :: Shit fix
                        return null;
                    int nextIndex = closestIndex + this.direction;
                    if (nextIndex < 0) nextIndex = pointsSize - 1;
                    else if (nextIndex >= pointsSize) nextIndex = 0;

                    nextPoint = points.get(nextIndex);

                    if (!nextPoint.valid)
                        this.direction = -this.direction;
                    ++passes;
                } while (!nextPoint.valid);

                return nextPoint;
        }
    }

    private void collectPoints(final int size,
                               final double radius,
                               final EntityLivingBase entity) {
        this.currentPoints.clear();

        final double x = entity.posX;
        final double z = entity.posZ;

        final double pix2 = Math.PI * 2.0;

        for (int i = 0; i < size; i++) {
            double cos = radius * StrictMath.cos(i * pix2 / size);
            double sin = radius * StrictMath.sin(i * pix2 / size);

            final Point point = new Point(entity,
                    new Vec3(cos, 0, sin),
                    this.validatePoint(new Vec3(x + cos, entity.posY, z + sin)));

            this.currentPoints.add(point);
        }
    }

    private static Point getClosestPoint(final double srcX, final double srcZ, List<Point> points) {
        double closest = Double.MAX_VALUE;
        Point bestPoint = null;

        for (Point point : points) {
            if (point.valid) {
                final double dist = MathUtil.getDistance(srcX, srcZ, point.point.xCoord, point.point.zCoord);
                if (dist < closest) {
                    closest = dist;
                    bestPoint = point;
                }
            }
        }

        return bestPoint;
    }

    private boolean validatePoint(final Vec3 point) {
        final EntityPlayer player = mc.thePlayer;
        final WorldClient world = mc.theWorld;

        final MovingObjectPosition rayTraceResult = mc.theWorld.rayTraceBlocks(player.getPositionVector(), point,
                false, true, false);

        if (rayTraceResult != null && rayTraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            return false;

        // TODO :: Replace this with bb check

        final BlockPos pointPos = new BlockPos(point);
        final IBlockState blockState = world.getBlockState(pointPos);

        if (blockState.getBlock().canCollideCheck(blockState, false) && !blockState.getBlock().isPassable(mc.theWorld, pointPos))
            return false;

        final IBlockState blockStateAbove = world.getBlockState(pointPos.add(0, 1, 0));

        return !blockStateAbove.getBlock().canCollideCheck(blockState, false) &&
                !isOverVoid(point.xCoord, Math.min(point.yCoord, mc.thePlayer.posY), point.zCoord);
    }

    private boolean isOverVoid(final double x,
                               final double y,
                               final double z) {
        for (double posY = y; posY > 0.0; posY--) {
            final IBlockState state = mc.theWorld.getBlockState(new BlockPos(x, posY, z));
            if (state.getBlock().canCollideCheck(state, false)) {
                return y - posY > 2;
            }
        }

        return true;
    }

    public boolean isCloseToPoint(final Point point) {
        return MathUtil.getDistance(mc.thePlayer.posX, mc.thePlayer.posZ, point.point.xCoord, point.point.zCoord) < 0.2;
    }

    public boolean shouldAdaptSpeed() {
        if (!this.adaptiveSpeedProperty.getValue())
            return false;
        return this.isCloseToPoint(this.currentPoint);
    }

    public double getAdaptedSpeed() {
        final EntityLivingBase entity = this.currentTarget;
        if (entity == null) return 0.0;
        return MathUtil.getDistance(entity.prevPosX, entity.prevPosZ, entity.posX, entity.posZ);
    }

    public boolean shouldStrafe() {
        return this.toggled() &&
                (!this.holdSpaceProperty.getValue() || Keyboard.isKeyDown(Keyboard.KEY_SPACE)) &&
                this.currentTarget != null &&
                this.currentPoint != null;
    }

    public void setSpeed(final MovePlayerEvent event, final double speed) {
        final EntityPlayerSP player = mc.thePlayer;
        final Point point = this.currentPoint;
        MovementUtil.setSpeed(event, speed, 1, 0,
                PlayerUtil.calculateYawFromSrcToDst(player.rotationYaw,
                        player.posX, player.posZ,
                        point.point.xCoord, point.point.zCoord));
    }

    @Override
    public void onEnable() {
        if (this.aura == null) {
            this.aura = KillAuraModule.instance();
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.currentPoints.clear();
        super.onDisable();
    }

    private enum Mode {
        BEHIND,
        FOLLOW,
        CIRCLE
    }

    private enum RenderMode {
        OFF,
        POINTS,
        POLYGON
    }

    public static class Point {
        private final EntityLivingBase entity;
        private final Vec3 posOffset;
        public Vec3 point;
        private final boolean valid;

        public Point(final EntityLivingBase entity,
                     final Vec3 posOffset,
                     final boolean valid) {
            this.entity = entity;
            this.posOffset = posOffset;
            this.valid = valid;

            this.point = this.calculatePos();
        }

        public Vec3 getPoint() {
            return point;
        }

        private Vec3 calculatePos() {
            return this.entity.getPositionVector().add(this.posOffset);
        }

        private Vec3 calculateInterpolatedPos(final float partialTicks) {
            final double x = RenderUtil.interpolate(this.entity.posX, this.entity.prevPosX, partialTicks);
            final double y = RenderUtil.interpolate(this.entity.posY, this.entity.prevPosY, partialTicks);
            final double z = RenderUtil.interpolate(this.entity.posZ, this.entity.prevPosZ, partialTicks);

            final Vec3 interpolatedEntity = new Vec3(x, y, z);

            return interpolatedEntity.add(this.posOffset);
        }
    }
}
