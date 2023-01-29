package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.EAGames;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.MovePlayerEvent;
import me.dinozoid.strife.event.implementations.player.PlayerJumpEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.player.PlayerStrafeEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.module.implementations.combat.TargetStrafeModule;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.player.RotationUtil;
import me.dinozoid.strife.util.system.MathUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;

import static me.dinozoid.strife.util.network.PacketUtil.sendPacketNoEvent;

@ModuleInfo(name = "Flight", renderName = "Flight", description = "Fly like a bird.", aliases = "Fly", category = Category.MOVEMENT)
public class FlightModule extends Module {

    private final EnumProperty<FlightMode> modeProperty = new EnumProperty<>("Mode", FlightMode.VANILLA);
    private final DoubleProperty flightSpeedProperty = new DoubleProperty("Flight Speed", 5, 1, 5, 0.1);
    private final Deque<Packet> packets = new ArrayDeque<>();
    private final TimerUtil timerUtil = new TimerUtil();
    private BlockPos blockPos;
    private double startPosY;
    private int hypixelStage, stage, bestBlockStack;
    private boolean set, doFly;

    @EventHandler
    private final Listener<EAGames> eaGamesListener = new Listener<>(event ->{
        if (modeProperty.getValue() == FlightMode.HYPIXEL_NEW && !doFly && bestBlockStack != -1 && bestBlockStack >= 36)
            event.setCurrentItem(bestBlockStack - PlayerUtil.ONLY_HOT_BAR_BEGIN);
    });

    @EventHandler
    private final Listener<PlayerStrafeEvent> movePlayerListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case VANILLA: {
                mc.thePlayer.motionY = mc.gameSettings.keyBindJump.isKeyDown() ? 0.42 : mc.gameSettings.keyBindSneak.isKeyDown() ? -0.42 : -0.0625;
                event.setMotion(flightSpeedProperty.getValue());
                if (!MovementUtil.isMoving()) mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                break;
            }
            case DASH: {
                //MovementUtil.setSpeed(event, 0);
                break;
            }
            case HYPIXEL_NEW: {
//                if(hypixelStage < 2){
//                    event.setX(0);
//                    event.setZ(0);
//                    return;
//                }
                if(set){
                    TargetStrafeModule strafeModule = Client.INSTANCE.getModuleRepository().moduleBy(TargetStrafeModule.class);

                    if(strafeModule.shouldStrafe()){
                        event.setYaw(PlayerUtil.calculateYawFromSrcToDst(mc.thePlayer.rotationYaw, mc.thePlayer.posX, mc.thePlayer.posZ, strafeModule.currentPoint.point.xCoord, strafeModule.currentPoint.point.zCoord));
                    }

                    //event.setMotionPartialStrafe((float) (MovementUtil.getBaseMoveSpeed() - MovementUtil.), 0.2375F + MovementUtil.getRandomHypixelValuesFloat());
                }
                break;
            }
            case BLIRTZISSODADDYTHATDINOZOIDWILLKEEPTHISMODE:{
                if(MovementUtil.isMovingOnGround()){
                    TargetStrafeModule strafeModule = Client.INSTANCE.getModuleRepository().moduleBy(TargetStrafeModule.class);

                    if(strafeModule.shouldStrafe()){
                        event.setYaw(PlayerUtil.calculateYawFromSrcToDst(mc.thePlayer.rotationYaw, mc.thePlayer.posX, mc.thePlayer.posZ, strafeModule.currentPoint.point.xCoord, strafeModule.currentPoint.point.zCoord));
                    }

                    if (MovementUtil.isMoving() && MovementUtil.isOnGround() && !this.mc.thePlayer.isCollidedHorizontally) {
                        this.mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(this.mc.thePlayer.posX + mc.thePlayer.motionX, this.mc.thePlayer.posY, this.mc.thePlayer.posZ + mc.thePlayer.motionZ, true));
                        if(strafeModule.shouldStrafe()){
                            mc.thePlayer.motionX = mc.thePlayer.motionX * 1;
                            mc.thePlayer.motionZ = mc.thePlayer.motionZ * 1;
                        }else {
                            mc.thePlayer.motionX = mc.thePlayer.motionX * 2;
                            mc.thePlayer.motionZ = mc.thePlayer.motionZ * 2;
                        }
                        event.setMotionPartialStrafe((float) (MovementUtil.getBaseMoveSpeed() * (strafeModule.shouldStrafe() ? 1 : 2)), 0.2375F + MovementUtil.getRandomHypixelValuesFloat());
                    }

//                    if(strafeModule.shouldStrafe()){
//                        event.setYaw(PlayerUtil.calculateYawFromSrcToDst(mc.thePlayer.rotationYaw, mc.thePlayer.posX, mc.thePlayer.posZ, strafeModule.currentPoint.point.xCoord, strafeModule.currentPoint.point.zCoord));
//                    }
                    //
                }
                break;
            }
        }
    });

    @EventHandler
    public final Listener<PacketInboundEvent> packetInboundEventListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case VANILLA: {
//                if (event.getPacket() instanceof S08PacketPlayerPosLook) {
//                    S08PacketPlayerPosLook packet = event.getPacket();
//                    event.cancel();
//                    sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(packet.getX(), packet.getY(), packet.getZ(), mc.thePlayer.onGround));
//                }
                break;
            }
            case HYPIXEL_NEW: {
                if(event.getPacket() instanceof S08PacketPlayerPosLook){
                    //set = true;
                }
//                if (event.getPacket() instanceof S27PacketExplosion) {
//                    S27PacketExplosion s27 = event.getPacket();
//                    serverPos = true;
//                    PlayerUtil.sendMessageWithPrefix("Damaged");
//                    toggle();
//                }
                break;
            }
        }
    });

    @EventHandler
    private final Listener<PacketOutboundEvent> packetOutboundListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case HYPIXEL_NEW: {
//                if (event.getPacket() instanceof C03PacketPlayer) {
//                    C03PacketPlayer c03 = event.getPacket();
//                    event.setCancelled(true);
//                    if (c03.isMoving()) {
//                        packets.add(c03);
//                    }
//                }
                break;
            }
        }
    });

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case DASH: {
                mc.thePlayer.motionY = mc.thePlayer.ticksExisted % 4 == 0 ? 0.001 : -0.001;
                if (timerUtil.hasElapsed(1150)) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX + MovementUtil.yawPos(7.99 + Math.random() / 500)[0], mc.thePlayer.posY - 1.7525 + Math.random() / 500, mc.thePlayer.posZ + MovementUtil.yawPos(7.99 + Math.random() / 500)[1]);
                    timerUtil.reset();
                }
                break;
            }
            case HYPIXEL_NEW: {
                if(mc.thePlayer.ticksExisted <= 5 ) return;
                    if(mc.thePlayer.getDistanceY(startPosY) > 1.05 && !doFly && event.isPost()){
                        // Perform ray trace with current angle stepped rotations
                        Vec3 hitVec;
                        final MovingObjectPosition rayTraceResult = RotationUtil.rayTraceBlocks(mc,
                                event.isPre() ? mc.thePlayer.lastReportedYaw : 0,
                                event.isPre() ? mc.thePlayer.lastReportedPitch : 90);
                        // If nothing is hit return
                        if (rayTraceResult == null) return;
                        // If did not hit block return
                        if (rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
                        // If side hit does not match block data return
                        if (rayTraceResult.sideHit != EnumFacing.UP) return;
                        // If block pos does not match block data return
                        final BlockPos dstPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.2, mc.thePlayer.posZ);
                        final BlockPos rayDstPos = rayTraceResult.getBlockPos();
                        if (rayDstPos.getX() != dstPos.getX() ||
                                rayDstPos.getY() != dstPos.getY() ||
                                rayDstPos.getZ() != dstPos.getZ()) return;

                        hitVec = rayTraceResult.hitVec;
                        bestBlockStack = getBestBlockStack(PlayerUtil.ONLY_HOT_BAR_BEGIN, PlayerUtil.END);
                        //mc.thePlayer.motionY = 0;
                        //PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, event.getYaw(), 90, true));
                        if(new BlockPos(event.getPosX(), mc.thePlayer.posY - 1, event.getPosZ()).getBlock() instanceof BlockAir) {
                            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventoryContainer.getSlot(bestBlockStack).getStack(), new BlockPos(event.getPosX(), mc.thePlayer.posY - 1.2, event.getPosZ()),
                                   EnumFacing.UP, hitVec)) {
                                mc.thePlayer.swingItem();
                                doFly = true;
                            }
                            //mc.rightClickMouse();
                           // PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(event.getPosX(), event.getPosY() - 1, event.getPosZ()), 255, mc.thePlayer.getHeldItem(), 0, 0, 0));
                        }
                        //PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    }

                if(mc.thePlayer.onGround){
                    mc.thePlayer.jump();
                }

//                if(hypixelStage >= 2 && set && event.isPost()){
//                    double x = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
//                    double z = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
//                    PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, false));
//                    mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
//                }

                if(event.isPre()) {
                  if(!doFly) event.setPitch(90);


                    if (!doFly) {
                        mc.thePlayer.motionX *= mc.thePlayer.motionZ *= 0;
                        return;
                    }
                    hypixelStage++;

                    if (set) {
                        stage++;
                    }

                    double timer = 1.7f;

                    if (hypixelStage >= 2) {
                        MovementUtil.setSpeed(MovementUtil.getBaseMoveSpeed() * .938);
                        mc.thePlayer.motionY = 0;
                        event.setGround(true);
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() > 0) {
                            if (set && stage > 5) {
                                if(timerUtil.hasElapsed(50L)){
                                    mc.timer.timerSpeed = 7f;
                                    timerUtil.reset();
                                }else{
                                    mc.timer.timerSpeed = 0.99f;
                                }
                                //mc.timer.timerSpeed = mc.thePlayer.ticksExisted %  == 0 ? 8f : 0.99f;
                                //mc.timer.timerSpeed = 1.2f;
//                                if(mc.thePlayer.ticksExisted % 2 == 0){
//                                    mc.timer.timerSpeed = 1.0f;
//                                }else{
//                                    mc.timer.timerSpeed = 0.1f;
//                                    double x = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
//                                    double z = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
//                                    PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, false));
//                                    mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
//                                }
                               // mc.timer.timerSpeed = 1.5f;
                              //  mc.thePlayer.capabilities.isFlying = true;
//                                if(mc.timer.timerSpeed > 2){
//                                    mc.timer.timerSpeed = 2;
//                                }
//                                //if(mc.thePlayer.ticksExisted % 2 != 1) { mc.timer.timerSpeed = 1.5f + MovementUtil.getRandomHypixelValuesFloat();  } else  mc.timer.timerSpeed = (float) (0.9f + MovementUtil.getRandomHypixelValuesFloat());
                                //mc.timer.timerSpeed = (float) (stage < 20 ? 2.3 : stage < 60 && stage > 20 ? 1.9 : 1.6f);
                            } else {
                              //  mc.timer.timerSpeed =  (float)timer;
                            }
                        } else {
                            mc.timer.timerSpeed = 0.7f;
                        }
//                        if(set)mc.timer.timerSpeed = (float) Math.max(1.9 + MovementUtil.getRandomHypixelValuesFloat(), timer);
                    }
//                        switch (hypixelStage){
//                            case 1:{
//                                mc.thePlayer.motionY = 0.05F;
//                                break;
//                            }
//                            case 5:{
//                                event.setPosY(event.getPosY() - 0.22);
//                                mc.thePlayer.posY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
//                                mc.thePlayer.lastTickPosY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
//                                break;
//                            }
//                        }
                    if (!set) {
                        switch (hypixelStage){
                            case 1:{
                                mc.thePlayer.motionY = 1 / 64;
                                break;
                            }
                            case 3:{
                                mc.thePlayer.posY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                                mc.thePlayer.lastTickPosY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                                break;
                            }
                            case 4: {
                                mc.thePlayer.motionY = -0.489989898;
                                mc.thePlayer.posY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                                mc.thePlayer.lastTickPosY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                                break;
                            }//mc.thePlayer.motionY = -0.489;
                            case 5:{
                              //  mc.thePlayer.motionY = 0.489;
                                event.setPosY(event.getPosY() - 1.3 + MovementUtil.getRandomHypixelValuesFloat());
                                mc.thePlayer.motionY = 0.489989898;
                                mc.thePlayer.posY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                                mc.thePlayer.lastTickPosY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
                                set = true;
                                // packets.add(new C0FPacketConfirmTransaction());
                                break;
                            }
                        }
                    }
                }

//                if(event.isPre() && set && mc.thePlayer.ticksExisted % 4 != 0){
//                    double x = (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * .7;
//                        double z = (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * .7;
//                        mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, true));
//                        mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
//                }

//                if(event.isUpdate()){
//                    if(set && !mc.thePlayer.onGround){
//                        double x = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
//                        double z = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
//                        mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, false));
//                        mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
//                    }
//                }
                break;
            }
            case BLIRTZISSODADDYTHATDINOZOIDWILLKEEPTHISMODE:{
                if(MovementUtil.isMoving()){
                    if(mc.thePlayer.onGround){
//                        if(event.isPre()){
//                            double[] gar = MovementUtil.yawPos(mc.thePlayer.getDirection(), MovementUtil.getBaseMoveSpeed());
//                            double x = event.getPosX() - event.getPrevPosX();
//                            double z = event.getPosZ() - event.getPrevPosZ();
//                            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(event.getPosX() - gar[0], event.getPosY(), event.getPosZ() - gar[1], event.isGround()));
//                            //mc.thePlayer.setPosition(event.getPosX() + x, event.getPosY(), event.getPosZ() + z);
//                        }
//                        if (MovementUtil.isMoving() && MovementUtil.isOnGround() && !this.mc.thePlayer.isCollidedHorizontally) {
//                            this.mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(this.mc.thePlayer.posX + event.getPosX(), this.mc.thePlayer.posY, this.mc.thePlayer.posZ + event.getPosZ(), true));
//                            event.setPosX(event.getPosX() * 2.0);
//                            event.setPosZ(event.getPosZ() * 2.0);
//                            MovementUtil.setSpeed( MovementUtil.getBaseMoveSpeed() * 2.0);
//                        }
                    }
                }
                break;
            }
        }
    });

    @EventHandler
    private final Listener<PlayerJumpEvent> jumpEventListener = new Listener<>(event -> {
       event.setCancelled(modeProperty.getValue() == FlightMode.BLIRTZISSODADDYTHATDINOZOIDWILLKEEPTHISMODE);
    });

    public static FlightModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(FlightModule.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        blockPos = mc.thePlayer.playerLocation;
        hypixelStage = 0;
        doFly = false;
        startPosY = mc.thePlayer.posY;
        set = false;
        packets.clear();

//        for (int i = 1; i <= 2; ++i) {
//            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.05, mc.thePlayer.posZ, false));
//            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
//        }

        stage = 0;

      //  packets.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.timer.timerSpeed = 1.0f;
       // PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        mc.thePlayer.capabilities.isFlying = false;
        stage = 0;
//        if (!packets.isEmpty()) {
//            PlayerUtil.sendMessageWithPrefix("work?");
//            packets.forEach(PacketUtil::sendPacketNoEvent);
//            packets.clear();
//        }
        mc.thePlayer.motionX *= mc.thePlayer.motionZ *= 0;
        set = false;
    }

    @Override
    public void init() {
        super.init();
        addValueChangeListener(modeProperty);
    }

    private enum FlightMode {
        VANILLA, DASH, HYPIXEL_NEW, BLIRTZISSODADDYTHATDINOZOIDWILLKEEPTHISMODE
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

}
