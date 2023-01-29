package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.MovePlayerEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "FunnyPacket", renderName = "Funny Packet", category = Category.MOVEMENT)
public class FunnyPacketModule extends Module {
    private final EnumProperty<LongJumpMode> modeProperty = new EnumProperty<>("Mode", LongJumpMode.SEMI_FLY);
    @EventHandler
    private final Listener<PacketOutboundEvent> eventListener = new Listener<>(event -> {

    });
    private final TimerUtil timer = new TimerUtil();
    private final List<Packet<?>> packets = new ArrayList<>();
    private boolean doDisabler;
    private int stage;
    private int disablerTicks;
    private double movementSpeed;
    @EventHandler
    private final Listener<MovePlayerEvent> movePlayerEventListener = new Listener<>(event -> {
        if (mc.thePlayer.hurtTime > 0) {
            doDisabler = true;
        }

        if (doDisabler) {
            double baseSpeed = MovementUtil.getBaseMoveSpeed();
            if (MovementUtil.isMoving()) {
                if (MovementUtil.isOnGround()) {
                    event.setY(mc.thePlayer.motionY = MovementUtil.getJumpHeight(2.5F));
                    stage = 0;
                }

                switch (stage) {
                    case 0: {
                        movementSpeed = baseSpeed * 2.135;
                        break;
                    }
                    case 1: {
                        //mc.timer.timerSpeed = 0.1f;
                        movementSpeed *= 0.25;
                        break;
                    }
                    case 4:
                    case 5: {
                        movementSpeed = baseSpeed * 8.6;
                        break;
                    }
                    default: {
//                        if(mc.thePlayer.fallDistance <= 0.05){
//                            event.y(mc.thePlayer.motionY += 0.005);
//                        }
                        movementSpeed -= movementSpeed / 3;
                        break;
                    }
                }
                this.stage++;
                MovementUtil.setSpeed(event, movementSpeed = Math.max(movementSpeed, baseSpeed));
            }
        }
    });
    private float motionMultiply;
    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerEventListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case SEMI_FLY: {
                mc.timer.timerSpeed = (doDisabler) ? 1.8f : 1.0f;
                if (timer.hasElapsed(150L)) {
                    doDisabler = false;
                    if (mc.thePlayer.onGround)
                        toggle();
                    timer.reset();
                }
                if (!doDisabler) {
                    ++disablerTicks;
                }
                if (!doDisabler) {
                    motionMultiply += motionMultiply / 7.0f + 1.0E-2F;
                    mc.thePlayer.motionY *= motionMultiply;
                }
                mc.timer.timerSpeed = 1.0f;
                if (movementSpeed < MovementUtil.getBaseMoveSpeed())
                    movementSpeed = (float) MovementUtil.getBaseMoveSpeed();
                if (motionMultiply > 0.89f)
                    motionMultiply = 0.89f;
                movementSpeed -= movementSpeed / 35.0f;
                break;
            }
            case NORMAL: {
                if (timer.hasElapsed(160L)) {
                    doDisabler = false;
                    timer.reset();
                }
                if (!doDisabler) mc.thePlayer.motionY *= 0.78f;
                movementSpeed -= movementSpeed / 25.0f;
                break;
            }
            case HYPIXEL_BW: {
                //mc.thePlayer.motionY += 0.00007045632643;

                break;
            }
        }
    });

    @Override
    public void onEnable() {
        super.onEnable();
        doDisabler = false;
        switch (modeProperty.getValue()) {
            case SEMI_FLY: {
                motionMultiply = 0.25f;
                movementSpeed = (float) (MovementUtil.getBaseMoveSpeed() * 1.89f);
                break;
            }
            case NORMAL: {
                damage();
                movementSpeed = (float) (MovementUtil.getBaseMoveSpeed() * 2.3f);
                break;
            }
            case HYPIXEL_BW: {
                movementSpeed = 0;
                break;
            }
        }
        timer.reset();
        mc.thePlayer.motionY = 0.42f;
    }

    public void damage() {
//        double x = mc.thePlayer.posX;
//        double y = mc.thePlayer.posY;
//        double z = mc.thePlayer.posZ;
//        for (int i = 0;i<MovementUtil.getMaxFallDist() + 2.0f;i++) {
////            PacketUtil.packetNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 9.9E-2F, z, false));
////            PacketUtil.packetNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x + 14.19E-2, y + 1.8E-1, z, false));
//            PacketUtil.packetNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.96771E-19, z, false));
//            PacketUtil.packetNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z + 2.0E-9F, false));
//            PacketUtil.packetNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x + 2.0E-9F, y, z, false));
//        }
//        PacketUtil.packetNoEvent(new C03PacketPlayer(true));
        MovementUtil.damage();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        packets.clear();
        mc.timer.timerSpeed = 1.0f;
        doDisabler = false;
        disablerTicks = 0;
        stage = 0;
    }

    private enum LongJumpMode {
        SEMI_FLY, NORMAL, HYPIXEL_BW
    }
}