package me.dinozoid.strife.module.implementations.player;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.BlockPlaceEvent;
import me.dinozoid.strife.event.implementations.EAGames;
import me.dinozoid.strife.event.implementations.player.MovePlayerEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.player.PlayerStrafeEvent;
import me.dinozoid.strife.event.implementations.player.WindowClickEvent;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import me.dinozoid.strife.event.implementations.render.Render3DEvent;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.font.FontRepository;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.module.implementations.movement.SafeWalkModule;
import me.dinozoid.strife.module.implementations.movement.SpeedModule;
import me.dinozoid.strife.newshader.blur.GaussianBlur;
import me.dinozoid.strife.newshader.blur.KawaseBlur;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.player.RotationUtil;
import me.dinozoid.strife.util.player.WindowClickRequest;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.render.RoundedUtil;
import me.dinozoid.strife.util.render.StencilUtil;
import me.dinozoid.strife.util.system.MathUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "Scaffold", renderName = "Scaffold", aliases = "BlockFly", description = "Place blocks under you.", category = Category.PLAYER)
public class ScaffoldModule extends Module {
    private static final EnumFacing[] FACINGS = new EnumFacing[]{
            EnumFacing.EAST,
            EnumFacing.WEST,
            EnumFacing.SOUTH,
            EnumFacing.NORTH};

    private double oPosY;
    private int slowTicks;

    // Tower
    private final Property<Boolean> towerProperty = new Property<Boolean>("Tower", true);
    // Draw
    //private final MultiSelectEnumProperty<DrawOption> drawOptionsProperty = new MultiSelectEnumProperty<DrawOption>("Draw Options", Lists.newArrayList(DrawOption.PLACEMENT), DrawOption.values());
    // Placement
    private final Property<Boolean> spoofHeldItemProperty = new Property<Boolean>("Spoof Held Item", true);
    private final DoubleProperty placeDelayProperty = new DoubleProperty("Place Delay", 0, 0, 10, 1);
    private final DoubleProperty expandProperty = new DoubleProperty("Expand", 0, 0.1, 10.0, 0.01);
    private final EnumProperty<Swing> swingProperty = new EnumProperty<>("Swing", Swing.SILENT);
    private final Property<Boolean> rayTraceCheckProperty = new Property<Boolean>("Ray Trace Check", false);
    // Movement
    private final Property<Boolean> autoJumpProperty = new Property<Boolean>("Auto Jump", false);
    private final Property<Boolean> safeWalkProperty = new Property<Boolean>("Safe Walk", false);
    private final Property<Boolean> keepPosProperty = new Property<Boolean>("No Y Gain", true);
    public final Property<Boolean> noSprintProperty = new Property<Boolean>("No Sprint", true);

    // For drawing only...
    private final List<Vec3> breadcrumbs = new ArrayList<>();
    private double fadeInOutProgress;
    private int totalBlockCount;
    // Counters
    private int ticksSinceWindowClick;
    private int ticksSincePlace;
    // Block data
    private BlockData data;
    private BlockData lastPlacement;
    public float[] angles;
    // Tower
    private boolean towering;
    private int placedBlocks;
    // Other...
    private SpeedModule speed;
    private int bestBlockStack;
    private double startPosY;
    private WindowClickRequest lastRequest;

    // Autojump
    private double moveSpeed;
    private double lastDist;
    private boolean wasOnGround;
    private float derp;

    @EventHandler
    private final Listener<EAGames> eaGamesListener = new Listener<>(event ->{
        if (spoofHeldItemProperty.getValue() && bestBlockStack != -1 && bestBlockStack >= 36)
            event.setCurrentItem(bestBlockStack - PlayerUtil.ONLY_HOT_BAR_BEGIN);
    });

    @EventHandler
    private final Listener<WindowClickEvent> onWindowClick = new Listener<>(event -> {
        ticksSinceWindowClick = 0;
    });

    @EventHandler
    private final Listener<BlockPlaceEvent> onBlockPlace = new Listener<>(event -> {
        ticksSincePlace = 0;
    });

    @EventHandler
    private final Listener<PlayerStrafeEvent> onMove = new Listener<>(event -> {
        final double baseMoveSpeed = MovementUtil.getBaseMoveSpeed(true);

        if (autoJumpProperty.getValue()) {
            if (MovementUtil.isMoving()) {
                if (mc.thePlayer.onGround && !wasOnGround) {
                    moveSpeed = baseMoveSpeed;
                    mc.thePlayer.motionY = MovementUtil.getJumpHeight(0.42F);
                    wasOnGround = true;
                } else if (wasOnGround) {
                    wasOnGround = false;
                    final double bunnySlope = 0.66 * (lastDist - baseMoveSpeed);
                    moveSpeed = lastDist - bunnySlope;
                } else {
                    moveSpeed = moveSpeed / 100 * 98.5f;
                }
                moveSpeed = MovementUtil.getBaseMoveSpeed();
                event.setMotionPartialStrafe((float) (mc.thePlayer.ticksExisted % 3 == 0 ? moveSpeed * .75 : moveSpeed - 0.01), (float) (0.2375F + Math.random() / 500));
               // MovementUtil.setSpeed(event, mc.thePlayer.ticksExisted % 3 == 0 ? moveSpeed * .75 : moveSpeed - 0.01);
            }
        }
    });

    @EventHandler
    private final Listener<PlayerMotionEvent> playerMotionEventListener = new Listener<>(event -> {
        if(!MovementUtil.isMoving()) oPosY = event.getPosY();
        if(mc.thePlayer.posY > oPosY && autoJumpProperty.getValue()){
            EntityPlayer.enableCameraYOffset = true;
            EntityPlayer.cameraYPosition = oPosY;
        }


        if(event.isPre()) {
            if (towering) {
                if (Client.INSTANCE.getModuleRepository().moduleBy(TimerModule.class).toggled())
                    mc.timer.timerSpeed = 1.0f;
                if ((!MovementUtil.isMoving() || this.towerProperty.getValue())) {
                    if (this.mc.theWorld.getBlockState(new BlockPos(this.mc.thePlayer).add(0, 2, 0)).getBlock() instanceof BlockAir) {
                        this.mc.thePlayer.cameraPitch = 0.0f;
                        final double[] jumpY = {0.41999998688698, 0.7531999805212};
                        final double divideY = event.getPosY() % 1.0;
                        final double roundY = MathHelper.floor_double(this.mc.thePlayer.posY);
                        if (divideY > 0.419 && divideY < 0.753) {
                            event.setPosY(roundY + jumpY[0]);
                        } else if (divideY > 0.753) {
                            event.setPosY(roundY + jumpY[1]);
                        } else {
                            event.setPosY(roundY);
                            event.setGround(true);
                        }
                        if (!MovementUtil.isMoving()) {
                            RandomUtils.nextDouble(0.06, 0.0625);
                        }
                    }
                }
            }
        } else if (MovementUtil.isOnGround(0.15) && this.mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.thePlayer.motionX *= 0.8;
                    mc.thePlayer.motionZ *= 0.8;
                    this.mc.thePlayer.motionY = 0.41999976;
                }
//                if (MovementUtil.isOnGround(0.7)) {
//                    double n = event.getPosY() % 1.0;
//                    double n2 = down(event.getPosY());
//                    List<Object> list = Arrays.asList(new Double[]{0.419 + MovementUtil.getRandomHypixelValuesFloat(), 0.7539985435435 + MovementUtil.getRandomHypixelValuesFloat()});
//                    if (n > 0.419 && n < 0.753) {
//                        event.setPosY(n2 + (Double) list.get(0));
//                    } else if (n > 0.753) {
//                        event.setPosY(n2 + (Double) list.get(1));
//                    } else {
//                        mc.thePlayer.motionY = 0.419 + MovementUtil.getRandomHypixelValuesFloat();
//                        event.setPosY(n2);
//                    }
//                    if (!MovementUtil.isMoving()) {
//                        // if(timerSpeedProperty.getValue()) mc.timer.timerSpeed = (float) (2.7 + (Math.random() / 50L));
//                        mc.thePlayer.motionX = 0;
//                        mc.thePlayer.motionZ = 0;
//                        //event.setPosX(event.getPosX() + MovementUtil.yawPos((mc.thePlayer.ticksExisted % 2 != 0 ? ThreadLocalRandom.current().nextDouble(0.06 + MovementUtil.getRandomHypixelValuesFloat(), 0.0625 + MovementUtil.getRandomHypixelValuesFloat()) : -ThreadLocalRandom.current().nextDouble(0.06 + MovementUtil.getRandomHypixelValuesFloat(),  0.0625 + MovementUtil.getRandomHypixelValuesFloat())))[0]);
//                        event.setPosX(event.getPosX() + (mc.thePlayer.ticksExisted % 2 == 0 ? ThreadLocalRandom.current().nextDouble(0.0834, 0.1) : -ThreadLocalRandom.current().nextDouble(0.0834, 0.1)));
//                        //event.setPosZ(event.getPosZ() + MovementUtil.yawPos((mc.thePlayer.ticksExisted % 2 != 0 ? ThreadLocalRandom.current().nextDouble(0.06 + MovementUtil.getRandomHypixelValuesFloat(),  0.0625 + MovementUtil.getRandomHypixelValuesFloat()) : -ThreadLocalRandom.current().nextDouble(0.06 + MovementUtil.getRandomHypixelValuesFloat(), 0.0625 + MovementUtil.getRandomHypixelValuesFloat())))[1]);
//                        event.setPosZ(event.getPosZ() + (mc.thePlayer.ticksExisted % 2 != 0 ? ThreadLocalRandom.current().nextDouble(0.0834, 0.1) : -ThreadLocalRandom.current().nextDouble(0.0834, 0.1)));
//                    }
//                }

        if (event.isPre()) {

            if (slowTicks <= 3 && mc.thePlayer.onGround && !noSprintProperty.getValue()) {
                final double[] xz = MovementUtil.yawPos(mc.thePlayer.getDirection(), MovementUtil.getBaseMoveSpeed() / 2);
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX - xz[0], mc.thePlayer.posY, mc.thePlayer.posZ - xz[1], true));
                slowTicks--;
            }

            if (autoJumpProperty.getValue()) {
                final double xDist = mc.thePlayer.lastTickPosX - event.getPosX();
                final double zDist = mc.thePlayer.lastTickPosZ - event.getPosZ();
                lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                event.setGround(true);
            }

            // Increment tick counters
            ticksSinceWindowClick++;
            ticksSincePlace++;

            // Invalidate old data
            data = null;

            // Update towering state
            towering = towerProperty.getValue() && mc.gameSettings.keyBindJump.isKeyDown();

            // Look for best block stack in hot bar
            bestBlockStack = getBestBlockStack(PlayerUtil.ONLY_HOT_BAR_BEGIN, PlayerUtil.END);

            calculateTotalBlockCount();
            moveBlocksIntoHotBar();

//            if(mc.thePlayer.onGround)
//                slowTicks = 0;
//            else
//                slowTicks++;

            // If best block stack is in hot bar
            if (bestBlockStack >= PlayerUtil.ONLY_HOT_BAR_BEGIN) {
                final BlockPos blockUnder = getBlockUnder();
                data = getBlockData(blockUnder);

                if (data == null) data = getBlockData(blockUnder.offset(EnumFacing.DOWN));

                if (data != null) {
                    // If ray trace fails hit vec will be null
                    if (validateReplaceable(data) && data.hitVec != null) {
                        // Calculate rotations to hit vec
                        angles = RotationUtil.getRotations(new float[]{mc.thePlayer.lastReportedYaw, mc.thePlayer.lastReportedPitch},
                                15.5f, RotationUtil.getHitOrigin(mc.thePlayer), data.hitVec);
                    } else {
                        data = null;
                    }
                }

                // If using no sprint & on ground
                if (noSprintProperty.getValue() && mc.thePlayer.onGround && !autoJumpProperty.getValue()) {
                    // And has speed effect...
                    final PotionEffect speed = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed);
                    final int moveSpeedAmp = speed == null ? 0 : speed.getAmplifier() + 1;
                    if (moveSpeedAmp > 0) {
                        final double multiplier = 1.0 + 0.2 * moveSpeedAmp + 0.1;
                        // Reduce motionX/Z based on speed amplifier
                        mc.thePlayer.motionX /= multiplier;
                        mc.thePlayer.motionZ /= multiplier;
                    }
                }


                // If has not set angles or has not yet placed a block
                if (angles == null || lastPlacement == null) {
                    // Get the last rotations (EntityPlayerSP#rotationYaw/rotationPitch)
                    final float[] lastAngles = this.angles != null ? this.angles : new float[]{event.getYaw(), event.getPitch()};
                    // Get the opposite direct that you are moving
                    final float moveDir = MovementUtil.getMovementDirection();
                    // Desired rotations
                    final float[] dstRotations = new float[]{moveDir + MathUtil.randomFloat(178, 180), 87.5f + MovementUtil.getRandomHypixelValuesFloat()};
                    // Smooth to opposite
                    RotationUtil.applySmoothing(lastAngles, 15.5f, dstRotations);
                    // Apply GCD fix (just for fun)
                    // RotationUtil.applyGCD(dstRotations, lastAngles);
                    angles = dstRotations;
                }

                // Set rotations to persistent rotations
                event.setYaw(angles[0]);
                event.setPitch(angles[1]);
            }
        } else {
           // if(!isOnEdge(0.25)) return;
            doPlace(event);
        }
    });

    private boolean isOnEdge(final double verbose) {
        final WorldClient world = mc.theWorld;
        final EntityPlayerSP player = mc.thePlayer;
        double[] gars = new double[] {0, verbose, -verbose};
        for (double x : gars) {
            for (double z : gars) {
                final BlockPos belowBlockPos = new BlockPos(player.posX + x, getBlockUnder().getY(), player.posZ + z);
                if (!(world.getBlockState(belowBlockPos).getBlock() instanceof BlockAir))
                    return false;
            }
        }
        return true;
    }

    private int down(double n) {
        int n2 = (int)n;
        try {
            if (n < (double)n2) {
                return n2 - 1;
            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        return n2;
    }

    public double[] getExpandCoords(double y) {
        BlockPos underPos = new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ);
        Block underBlock = mc.theWorld.getBlockState(underPos).getBlock();
        MovementInput movementInput = mc.thePlayer.movementInput;
        float forward = movementInput.moveForward, strafe = movementInput.moveStrafe, yaw = mc.thePlayer.rotationYaw;
        double xCalc = -999, zCalc = -999, dist = 0, expandDist = 0.1;

        while (!isAirBlock(underBlock)) {
            xCalc = mc.thePlayer.posX;
            zCalc = mc.thePlayer.posZ;
            dist++;
            if (dist > expandDist) dist = expandDist;
            xCalc += (forward * 0.45 * MathHelper.cos((float) Math.toRadians(yaw + 90.0f)) + strafe * 0.45 * MathHelper.sin((float) Math.toRadians(yaw + 90.0f))) * dist;
            zCalc += (forward * 0.45 * MathHelper.sin((float) Math.toRadians(yaw + 90.0f)) - strafe * 0.45 * MathHelper.cos((float) Math.toRadians(yaw + 90.0f))) * dist;
            if (dist == expandDist) break;
            underPos = new BlockPos(xCalc, y, zCalc);
            underBlock = mc.theWorld.getBlockState(underPos).getBlock();
        }

        return new double[]{xCalc, zCalc};
    }

    public boolean isAirBlock(Block block) {
        if (block.getMaterial().isReplaceable()) {
            return !(block instanceof BlockSnow) || !(block.getBlockBoundsMaxY() > 0.125);
        }

        return false;
    }


    private void doPlace(final PlayerMotionEvent event) {
        if (bestBlockStack < 36 || data == null || ticksSincePlace <= placeDelayProperty.getValue())
            return;

        final Vec3 hitVec;

        if (rayTraceCheckProperty.getValue()) {
            // Perform ray trace with current angle stepped rotations
            final MovingObjectPosition rayTraceResult = RotationUtil.rayTraceBlocks(mc,
                    event.isPre() ? mc.thePlayer.lastReportedYaw : event.getYaw(),
                    event.isPre() ? mc.thePlayer.lastReportedPitch : event.getPitch());
            // If nothing is hit return
            if (rayTraceResult == null) return;
            // If did not hit block return
            if (rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
            // If side hit does not match block data return
            if (rayTraceResult.sideHit != data.face) return;
            // If block pos does not match block data return
            final BlockPos dstPos = data.pos;
            final BlockPos rayDstPos = rayTraceResult.getBlockPos();
            if (rayDstPos.getX() != dstPos.getX() ||
                    rayDstPos.getY() != dstPos.getY() ||
                    rayDstPos.getZ() != dstPos.getZ()) return;

            hitVec = rayTraceResult.hitVec;
        } else {
            hitVec = data.hitVec;
        }

        final ItemStack heldItem;

        if (spoofHeldItemProperty.getValue()) {
            heldItem = mc.thePlayer.inventoryContainer.getSlot(bestBlockStack).getStack();
        } else {
            // Switch item client side
            mc.thePlayer.inventory.currentItem = bestBlockStack - PlayerUtil.ONLY_HOT_BAR_BEGIN;
            heldItem = mc.thePlayer.getCurrentEquippedItem();
        }

        if (heldItem == null) return;

        // Attempt place using ray trace hit vec
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, heldItem, data.pos, data.face, hitVec)) {
            //slowTicks++;
            derp += 45;
            lastPlacement = data;
            placedBlocks++;
            slowTicks = 3;
            // Save hit vec for bread crumbs

            switch (swingProperty.getValue()) {
                case CLIENT:
                    mc.thePlayer.swingItem();
                    break;
                case SILENT:
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C0APacketAnimation());
                    break;
            }
        }
    }


    @EventHandler
    private final Listener<Render2DEvent> onRenderGameOverlay = new Listener<>(event -> {
        final ScaledResolution sr = event.getScaledResolution();
        final float mx = sr.getScaledWidth() / 2.0f;
        final float my = sr.getScaledHeight() / 2.0f;

        final EntityPlayerSP player = mc.thePlayer;

        final double minFadeInProgress = 0.7;

        // Block counter
        if (bestBlockStack != -1) {
            // Get the "best" block itemStack from the inventory (computed every tick)
            final ItemStack stack = player.inventoryContainer.getSlot(bestBlockStack).getStack();
            // Check the stack in slot has not changed since last update
            if (stack != null) {
                if (fadeInOutProgress < 1.0)
                    fadeInOutProgress += 1.0 / Minecraft.getDebugFPS() * 2;

                final String blockCount = String.format(totalBlockCount == 1 ? "1 block" : "%s blocks", totalBlockCount);
                final FontRenderer fontRenderer = mc.fontRendererObj;

                final double width = 60 + (int) Math.ceil(fontRenderer.getStringWidth(blockCount)) / 2.7;
                final double height = 20;

                final double left = mx - width / 2.0;
                final double top = my + 20 + 10; // middle + arrow spacing + size

                // Background
                glPushMatrix();
                StencilUtil.initStencilToWrite();
                RoundedUtil.drawRoundOutline((float) left, (float) top, (float) width, (float) height, 8, 0.5f, new Color(30, 30, 30, 120), new Color(209, 50, 50));
                StencilUtil.readStencilBuffer(1);
                GaussianBlur.renderBlur(10);
                StencilUtil.uninitStencilBuffer();
                RoundedUtil.drawRoundOutline((float) left, (float) top, (float) width, (float) height, 8, 0.5f, new Color(30, 30, 30, 120), new Color(209, 50, 50));
                //RoundedUtil.drawRound((float) left, (float) top, (float) width, (float) height, 8, true, new Color(30, 30, 30, 120));


                final int itemStackSize = 16;

                final int textWidth = itemStackSize + 2 + (int) Math.ceil(fontRenderer.getStringWidth(blockCount));

                final int iconRenderPosX = (int) (left + width / 2 - textWidth / 2);

                final int iconRenderPosY = (int) (top + (height - itemStackSize) / 2);

                // Setup for item render with proper lighting
                final boolean restore = RenderUtil.glEnableBlend();
                GlStateManager.enableRescaleNormal();
                RenderHelper.enableGUIStandardItemLighting();

                // Draw block icon
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, iconRenderPosX, iconRenderPosY);

                // Restore after item render
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                glEnable(GL_ALPHA_TEST);
                RenderUtil.glRestoreBlend(restore);

                fontRenderer.drawString(blockCount, (float) (iconRenderPosX + itemStackSize + 2),
                        (float) (top + height / 2 - fontRenderer.FONT_HEIGHT / 2),
                        0xFFFFFFFF);
                glPopMatrix();
            }
        }
    });

    private static void addTriangleVertices(final double size) {
        glVertex2d(0, -size / 2);
        glVertex2d(-size / 2, size / 2);
        glVertex2d(size / 2, size / 2);
    }

    @Override
    public void onEnable() {
        lastPlacement = null;
        derp = 0;
        towering = false;
        slowTicks = 0;
        placedBlocks = 0;
        if(mc.thePlayer != null) mc.getNetHandler().sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
        oPosY = mc.thePlayer.posY;
        if (speed == null) {
            speed = Client.INSTANCE.getModuleRepository().moduleBy(SpeedModule.class);
        }
        if (autoJumpProperty.getValue()) {
            if (speed.toggled()) {
                speed.toggle();
            }
            moveSpeed = MovementUtil.getBaseMoveSpeed();
            lastDist = 0.0;
        }
        if (mc.thePlayer != null) startPosY = mc.thePlayer.posY;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        angles = null;
        if(mc.thePlayer != null) mc.getNetHandler().sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        breadcrumbs.clear();
        EntityPlayer.enableCameraYOffset = false;
        EntityPlayer.cameraYPosition = mc.thePlayer.posY;
        super.onDisable();
    }

    private BlockData getBlockData(final BlockPos pos) {
        final EnumFacing[] facings = FACINGS;

        // 1 of the 4 directions around player
        for (EnumFacing facing : facings) {
            final BlockPos blockPos = pos.add(facing.getOpposite().getDirectionVec());
            if (PlayerUtil.validateBlock(mc.theWorld.getBlockState(blockPos).getBlock(), PlayerUtil.BlockAction.PLACE_ON)) {
                final BlockData data = new BlockData(blockPos, facing);
                if (validateBlockRange(data))
                    return data;
            }
        }

        // 2 Blocks Under e.g. When jumping
        final BlockPos posBelow = pos.add(0, -1, 0);
        if (PlayerUtil.validateBlock(mc.theWorld.getBlockState(posBelow).getBlock(), PlayerUtil.BlockAction.PLACE_ON)) {
            final BlockData data = new BlockData(posBelow, EnumFacing.UP);
            if (validateBlockRange(data))
                return data;
        }

        // 2 Block extension & diagonal
        for (EnumFacing facing : facings) {
            final BlockPos blockPos = pos.add(facing.getOpposite().getDirectionVec());
            for (EnumFacing facing1 : facings) {
                final BlockPos blockPos1 = blockPos.add(facing1.getOpposite().getDirectionVec());
                if (PlayerUtil.validateBlock(mc.theWorld.getBlockState(blockPos1).getBlock(), PlayerUtil.BlockAction.PLACE_ON)) {
                    final BlockData data = new BlockData(blockPos1, facing1);
                    if (validateBlockRange(data))
                        return data;
                }
            }
        }

        return null;

    }

    private boolean validateBlockRange(final BlockData data) {
        final Vec3 pos = data.hitVec;

        if (pos == null)
            return false;

        final EntityPlayerSP player = mc.thePlayer;

        final double x = (pos.xCoord - player.posX);
        final double y = (pos.yCoord - (player.posY + player.getEyeHeight()));
        final double z = (pos.zCoord - player.posZ);

        final float reach = mc.playerController.getBlockReachDistance();

        return Math.sqrt(x * x + y * y + z * z) <= reach;
    }

    private boolean validateReplaceable(final BlockData data) {
        final BlockPos pos = data.pos.offset(data.face);
        return mc.theWorld.getBlockState(pos)
                .getBlock()
                .isReplaceable(mc.theWorld, pos);
    }

    private BlockPos getBlockUnder() {
        if (keepPosProperty.getValue() && !Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            boolean air = isAirBlock(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, Math.min(startPosY, mc.thePlayer.posY) - 1, mc.thePlayer.posZ)).getBlock());
            return new BlockPos(mc.thePlayer.posX, Math.min(startPosY, mc.thePlayer.posY) - 1, air ? mc.thePlayer.posZ : mc.thePlayer.posZ);
        } else {
            startPosY = mc.thePlayer.posY;

            boolean air1 = isAirBlock(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock());
            return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        }
    }

    private void moveBlocksIntoHotBar() {
        // If no blocks in hot bar
        if (ticksSinceWindowClick > 3) {
            // Look for best block stack in inventory
            final int bestStackInInv = getBestBlockStack(PlayerUtil.EXCLUDE_ARMOR_BEGIN, PlayerUtil.ONLY_HOT_BAR_BEGIN);
            // If you have no blocks return
            if (bestStackInInv == -1) return;

            boolean foundEmptySlot = false;

            for (int i = PlayerUtil.END - 1; i >= PlayerUtil.ONLY_HOT_BAR_BEGIN; i--) {
                final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                if (stack == null) {
                    if (lastRequest == null || lastRequest.isCompleted()) {
                        final int slotID = i;
                        PlayerUtil.queueClickRequest(lastRequest = new WindowClickRequest() {
                            @Override
                            public void performRequest() {
                                // Move blocks from inventory into free slot
                                PlayerUtil.windowClick(bestStackInInv,
                                        slotID - PlayerUtil.ONLY_HOT_BAR_BEGIN,
                                        PlayerUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                            }
                        });
                    }

                    foundEmptySlot = true;
                }
            }

            if (!foundEmptySlot) {
                if (lastRequest == null || lastRequest.isCompleted()) {
                    PlayerUtil.queueClickRequest(lastRequest = new WindowClickRequest() {
                        @Override
                        public void performRequest() {
                            final int overrideSlot = 9;
                            // Swap with item in last slot of hot bar
                            PlayerUtil.windowClick(bestStackInInv, overrideSlot,
                                    PlayerUtil.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                        }
                    });
                }
            }
        }
    }

    private int getBestBlockStack(final int start, final int end) {
        int bestSlot = -1, bestSlotStackSize = 0;

        for (int i = start; i < end; i++) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (stack != null &&
                    stack.stackSize > bestSlotStackSize &&
                    stack.getItem() instanceof ItemBlock &&
                    PlayerUtil.isStackValidToPlace(stack)) {

                bestSlot = i;
                bestSlotStackSize = stack.stackSize;
            }
        }

        return bestSlot;
    }

    private void calculateTotalBlockCount() {
        totalBlockCount = 0;

        for (int i = PlayerUtil.EXCLUDE_ARMOR_BEGIN; i < PlayerUtil.END; i++) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (stack != null &&
                    stack.stackSize >= 1 &&
                    stack.getItem() instanceof ItemBlock &&
                    PlayerUtil.isStackValidToPlace(stack)) {

                totalBlockCount += stack.stackSize;
            }
        }
    }

    private static class BlockData {

        private final BlockPos pos;
        private final EnumFacing face;
        private final Vec3 hitVec;

        public BlockData(BlockPos pos, EnumFacing face) {
            this.pos = pos;
            this.face = face;
            hitVec = calculateBlockData();
        }

        private Vec3 calculateBlockData() {
            final Vec3i directionVec = face.getDirectionVec();
            final Minecraft mc = Minecraft.getMinecraft();

            double x;
            double z;

            switch (face.getAxis()) {
                case Z:
                    final double absX = Math.abs(mc.thePlayer.posX);
                    double xOffset = absX - (int) absX;

                    if (mc.thePlayer.posX < 0) {
                        xOffset = 1.0F - xOffset;
                    }

                    x = directionVec.getX() * xOffset;
                    z = directionVec.getZ() * xOffset;
                    break;
                case X:
                    final double absZ = Math.abs(mc.thePlayer.posZ);
                    double zOffset = absZ - (int) absZ;

                    if (mc.thePlayer.posZ < 0) {
                        zOffset = 1.0F - zOffset;
                    }

                    x = directionVec.getX() * zOffset;
                    z = directionVec.getZ() * zOffset;
                    break;
                default:
                    x = 0.25;
                    z = 0.25;
                    break;
            }

            if (face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                x = -x;
                z = -z;
            }

            final Vec3 hitVec = new Vec3(pos).addVector(x + z, directionVec.getY() * 0.5, x + z);

            final Vec3 src = mc.thePlayer.getPositionEyes(1.0F);
            final MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(src,
                    hitVec,
                    false,
                    false,
                    true);

            if (obj == null || obj.hitVec == null || obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
                return null;

            switch (face.getAxis()) {
                case Z:
                    obj.hitVec = new Vec3(obj.hitVec.xCoord, obj.hitVec.yCoord, Math.round(obj.hitVec.zCoord));
                    break;
                case X:
                    obj.hitVec = new Vec3(Math.round(obj.hitVec.xCoord), obj.hitVec.yCoord, obj.hitVec.zCoord);
                    break;
            }

            if (face != EnumFacing.DOWN && face != EnumFacing.UP) {
                final IBlockState blockState = mc.theWorld.getBlockState(obj.getBlockPos());
                final Block blockAtPos = blockState.getBlock();

                double blockFaceOffset;

                blockFaceOffset = RandomUtils.nextDouble(0.1, 0.3);

                if (blockAtPos instanceof BlockSlab && !((BlockSlab) blockAtPos).isDouble()) {
                    final BlockSlab.EnumBlockHalf half = blockState.getValue(BlockSlab.HALF);

                    if (half != BlockSlab.EnumBlockHalf.TOP) {
                        blockFaceOffset += 0.5;
                    }
                }

                obj.hitVec = obj.hitVec.addVector(0.0D, -blockFaceOffset, 0.0D);
            }

            return obj.hitVec;
        }
    }

    private enum Swing {
        CLIENT,
        SILENT,
        NO_SWING;
    }

    public static ScaffoldModule getInstance(){
        return Client.INSTANCE.getModuleRepository().moduleBy(ScaffoldModule.class);
    }

    public boolean doSafeWalk(){
        return this.toggled() && safeWalkProperty.getValue() && !autoJumpProperty.getValue();
    }
}