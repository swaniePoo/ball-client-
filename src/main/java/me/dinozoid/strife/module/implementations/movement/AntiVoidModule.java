package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.system.MathUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "AntiVoid", renderName = "AntiVoid", description = "Don't fall into the void.", aliases = "AntiFall", category = Category.MOVEMENT)
public class AntiVoidModule extends Module {

    private final EnumProperty<AntiVoidMode> mode = new EnumProperty<>("Mode", AntiVoidMode.NORMAL);
    private final DoubleProperty fallDistance = new DoubleProperty("Distance", 3, 1, 10, 1, Property.Representation.INT);
    private final List<Packet> packets = new ArrayList<>();
    private double x, y, z;
    private boolean falling;

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        if (mc.thePlayer.capabilities.isFlying || mc.thePlayer.capabilities.allowFlying || Client.INSTANCE.getModuleRepository().moduleBy(FunnyPacketModule.class).toggled() || FlightModule.instance().toggled() || mc.thePlayer.ticksExisted <= 20)
            return;
        if (event.getState() == EventState.PRE) {
            switch (mode.getValue()) {
                case NORMAL:
                    if (!PlayerUtil.isBlockUnder() && mc.thePlayer.fallDistance > fallDistance.getValue()) {
                       event.setPosX(event.getPosX() + MathUtil.randomDouble(-0.3, 0.3));
                       event.setPosZ(event.getPosZ() + MathUtil.randomDouble(-0.3, 0.3));
                    }
                    break;
                case BLINK:
                    // Update safe coordinates
                    if (PlayerUtil.isBlockUnder() && mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround) {
                        x = mc.thePlayer.posX;
                        y = mc.thePlayer.posY;
                        z = mc.thePlayer.posZ;
                    }
                    // Teleport back to safe location
                    if (falling && mc.thePlayer.fallDistance >= fallDistance.getValue()) {
                        packets.clear();
                        mc.thePlayer.setPositionAndUpdate(x, y, z);
                        event.setPosX(x);
                        event.setPosY(y);
                        event.setPosZ(z);
                    }
                    break;
            }
        } else {

        }
    });
    @EventHandler
    private final Listener<PacketOutboundEvent> packetOutboundListener = new Listener<>(event -> {
        switch (mode.getValue()) {
            case NORMAL: {
                break;
            }
            case BLINK: {
                if (event.getPacket() instanceof C03PacketPlayer) {
                    if (!PlayerUtil.isBlockUnder()) {
                        // Player is falling client side
                        packets.add(event.getPacket());
                        event.cancel();
                        falling = true;
                    } else {
                        if (mc.thePlayer.fallDistance < fallDistance.getValue()) {
                            if (falling) {
                                for (Packet packet : new ArrayList<>(packets))
                                    PacketUtil.sendPacketNoEvent(packet);
                                packets.clear();
                                falling = false;
                            }
                        }
                    }
                }
                break;
            }
        }
    });
    @EventHandler
    private final Listener<PacketInboundEvent> packetInboundListener = new Listener<>(event -> {
        switch (mode.getValue()) {
            case NORMAL: {
                break;
            }
            case BLINK: {
                // Not a legitimate teleport
                if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                    S08PacketPlayerPosLook posLook = event.getPacket();
                    for (Packet packet : new ArrayList<>(packets)) {
                        C03PacketPlayer packetPlayer = (C03PacketPlayer) packet;
                        if (packetPlayer.getPositionX() == posLook.getX() && packetPlayer.getPositionY() == posLook.getY() && packetPlayer.getPositionZ() == posLook.getZ()) {
                            packets.clear();
                            falling = false;
                        }
                    }
                }
                break;
            }
        }
    });

    public boolean falling() {
        return falling;
    }

    public static AntiVoidModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(AntiVoidModule.class);
    }

    @Override
    public void init() {
        super.init();
        addValueChangeListener(mode);
    }

    private enum AntiVoidMode {
        NORMAL, BLINK
    }

}
