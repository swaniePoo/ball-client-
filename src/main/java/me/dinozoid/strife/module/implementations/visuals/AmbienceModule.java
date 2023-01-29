package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.event.implementations.system.TickEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

@ModuleInfo(name = "Ambience", renderName = "Ambience", category = Category.VISUALS)
public class AmbienceModule extends Module {

    private final DoubleProperty timeProperty = new DoubleProperty("Time", 0, 0, 24000, 100, Property.Representation.INT);

    @EventHandler
    private final Listener<TickEvent> tickListener = new Listener<>(event -> {
        if (mc.theWorld != null)
            mc.theWorld.setWorldTime(timeProperty.getValue().longValue());
    });

    @EventHandler
    private final Listener<PacketInboundEvent> packetInboundListener = new Listener<>(event -> {
        if (event.getPacket() instanceof S03PacketTimeUpdate) {
            event.cancel();
            if (mc.theWorld != null)
                mc.theWorld.setWorldTime(timeProperty.getValue().longValue());
        }
    });

}
