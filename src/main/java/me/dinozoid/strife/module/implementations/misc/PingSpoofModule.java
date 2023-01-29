package me.dinozoid.strife.module.implementations.misc;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.player.WorldLoadEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ModuleInfo(name = "Ping Spoof", description = "Spoofs your ping to other players", category = Category.MISC)
public class PingSpoofModule extends Module {

    private final DoubleProperty delay = new DoubleProperty("Delay", 1000, 10, 30000, 1);

    private final ConcurrentHashMap<Packet<?>, Long> packets = new ConcurrentHashMap<>();

    private final TimerUtil timer = new TimerUtil();

    @EventHandler
    private final Listener<WorldLoadEvent> worldLoadEventListener = new Listener<>(event -> {
        packets.clear();
    });

    @EventHandler
    private final Listener<PlayerMotionEvent> playerMotionEventListener = new Listener<>(event -> {
       if(event.isPre()){
           for (final Iterator<Map.Entry<Packet<?>, Long>> iterator = packets.entrySet().iterator(); iterator.hasNext(); ) {
               final Map.Entry<Packet<?>, Long> entry = iterator.next();

               if (entry.getValue() < System.currentTimeMillis()) {
                   PacketUtil.sendPacket(entry.getKey());
                   iterator.remove();
               }
           }
       }
    });

    @EventHandler
    private final Listener<PacketOutboundEvent> packetOutboundEventListener = new Listener<>(event -> {
        if (mc.isSingleplayer())
            return;

        final Packet<?> p = event.getPacket();

        if (p instanceof C0FPacketConfirmTransaction || p instanceof C00PacketKeepAlive) {
            packets.put(p, (long) (System.currentTimeMillis() + delay.getValue()));
            event.setCancelled(true);
        }
    });

    @EventHandler
    private final Listener<PacketInboundEvent> packetInboundEventListener = new Listener<>(event -> {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            timer.reset();
        }
    });

}
