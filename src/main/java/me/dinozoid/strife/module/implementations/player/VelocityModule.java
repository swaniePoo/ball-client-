package me.dinozoid.strife.module.implementations.player;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

@ModuleInfo(name = "Velocity", renderName = "Velocity", aliases = "Velo", category = Category.PLAYER)
public class VelocityModule extends Module {

    private final DoubleProperty horizontal = new DoubleProperty("Horizontal", 0, 0, 100, 5, Property.Representation.INT);
    private final DoubleProperty vertical = new DoubleProperty("Vertical", 0, 0, 100, 5, Property.Representation.INT);
    @EventHandler
    private final Listener<PacketInboundEvent> packetInboundEvent = new Listener<>(event -> {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet = event.getPacket();
            if (mc.theWorld != null && mc.thePlayer != null && packet.getEntityID() == mc.thePlayer.getEntityId()) {
                if (horizontal.getValue() == 0 && vertical.getValue() == 0) event.cancel();
                packet.setMotionX((int) (packet.getMotionX() * horizontal.getValue() / 100));
                packet.setMotionY((int) (packet.getMotionY() * vertical.getValue() / 100));
                packet.setMotionZ((int) (packet.getMotionZ() * horizontal.getValue() / 100));
            }
        }
        if (event.getPacket() instanceof S27PacketExplosion) {
            S27PacketExplosion packet = event.getPacket();
            if (mc.theWorld != null && mc.thePlayer != null) {
                if (horizontal.getValue() == 0 && vertical.getValue() == 0) event.cancel();
                packet.setX(packet.getX() / 100 * horizontal.getValue());
                packet.setY(packet.getY() / 100 * vertical.getValue());
                packet.setZ(packet.getZ() / 100 * horizontal.getValue());
            }
        }
    });

    @Override
    public void init() {
        super.init();
        setSuffix(horizontal.getValue().intValue() + "% " + vertical.getValue().intValue() + "%");
        horizontal.addValueChange((oldValue, value) -> setSuffix(value.intValue() + "% " + vertical.getValue().intValue() + "%"));
        vertical.addValueChange((oldValue, value) -> setSuffix(value.intValue() + "% " + horizontal.getValue().intValue() + "%"));
    }

}
