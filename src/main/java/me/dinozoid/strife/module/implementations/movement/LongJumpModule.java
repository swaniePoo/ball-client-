package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.MovePlayerEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.LinkedList;
import java.util.List;

@ModuleInfo(name = "LongJump", renderName = "LongJump", category = Category.MOVEMENT)
public class LongJumpModule extends Module {

    private final C07PacketPlayerDigging PLAYER_DIGGING = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
    private final C08PacketPlayerBlockPlacement BLOCK_PLACEMENT = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f);

    private final EnumProperty<LongJumpMode> modeProperty = new EnumProperty<>("Mode", LongJumpMode.HYPIXEL);
    private final Property<Boolean> autoBow = new Property<>("Auto Bow", true);

    private boolean damaged, charging;
    private double moveSpeed;
    private final List<Packet<?>> packets = new LinkedList<>();
    private int stage, chargeTicks;

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        if (event.getState() == EventState.PRE) {
            switch (modeProperty.getValue()) {
                case HYPIXEL: {
                    if (!charging && autoBow.getValue() && PlayerUtil.isHoldingBow()) {
                        PacketUtil.sendPacketNoEvent(BLOCK_PLACEMENT);
                        chargeTicks = 0;
                        charging = true;
                    } else {
                        switch (chargeTicks) {
                            case 1: {
                                event.setPitch(-90);
                                break;
                            }
                            case 2: {
                                PacketUtil.sendPacketNoEvent(PLAYER_DIGGING);
                            }
                        }
                        chargeTicks++;
                    }
                    break;
                }
            }
        }
    });

    @EventHandler
    private final Listener<PacketOutboundEvent> packetOutboundEventListener = new Listener<>(event -> {
       if(modeProperty.getValue() == LongJumpMode.HYPIXEL) {
           if (event.getPacket() instanceof C03PacketPlayer) {
               C03PacketPlayer packetPlayer = (C03PacketPlayer) event.getPacket();
               if (packetPlayer.isMoving()) {
                   packets.add(event.getPacket());
                   event.setCancelled(true);
               }
           }
           if (stage >= 9) {
               packets.forEach(packet -> mc.getNetHandler().sendPacketNoEvent(packet));
               packets.clear();
           }
       }
    });

    @EventHandler
    private final Listener<MovePlayerEvent> movePlayerListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case HYPIXEL: {
                if (stage < 8)
                    event.setY(mc.thePlayer.motionY = 0.45F);
                if (stage == 3 || stage == 4 || stage == 5)
                    event.setY(mc.thePlayer.motionY = 0.5F);
                stage++;
                if (MovementUtil.isMoving()) {
                    switch (stage) {
                        case 1:
                            MovementUtil.damage();
                            event.setY(mc.thePlayer.motionY = 0.42F);
                            if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
                                moveSpeed = MovementUtil.getBaseMoveSpeed() * 1.85;
                            }
                            break;

                        case 2:
                            event.setY(mc.thePlayer.motionY = 0.5F);
                            double difference = 0.66 * (moveSpeed - MovementUtil.getBaseMoveSpeed());
                            moveSpeed = moveSpeed - difference;
                            break;
                        default:
                            moveSpeed = moveSpeed / 100 * 98.5;
                            moveSpeed = Math.max(moveSpeed, MovementUtil.getBaseMoveSpeed());
                            break;
                    }
                    moveSpeed = Math.max(moveSpeed, MovementUtil.getBaseMoveSpeed());
                }
                // if (mc.thePlayer.motionY <= -0.) {

                mc.thePlayer.motionY += 0.029;
                // }
                MovementUtil.setSpeed(event, moveSpeed);
                if (mc.thePlayer.onGround && stage > 5)
                    toggle();
                break;
            }
        }
    });

    @Override
    public void init() {
        super.init();
        addValueChangeListener(modeProperty);
    }

    @Override
    public void onEnable() {
        super.onEnable();
//        if (MovementUtil.isOnGround() && !autoBow.getValue() && mc.thePlayer.hurtTime == 0) {
//            MovementUtil.damage();
//        }
        packets.clear();
        PacketUtil.packetTimesNoEvent(new C0CPacketInput(), 2);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        damaged = false;
        charging = false;
        moveSpeed = 0;
        if(!autoBow.getValue()){
            packets.forEach(packet -> mc.getNetHandler().sendPacketNoEvent(packet));
            packets.clear();
        }
        chargeTicks = 0;
        stage = 0;
        PacketUtil.packetTimesNoEvent(new C03PacketPlayer(false), 2);
    }


    public enum LongJumpMode {
        HYPIXEL
    }

}
