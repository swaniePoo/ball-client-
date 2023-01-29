package me.dinozoid.strife.module.implementations.combat;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.util.network.PacketUtil;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0BPacketEntityAction;

@ModuleInfo(name = "WTap", renderName = "WTap", category = Category.COMBAT)
public class WTapModule extends Module {

    @EventHandler
    private final Listener<PacketOutboundEvent> packetOutboundEvent = new Listener<>(event -> {
        if (event.getPacket() instanceof C02PacketUseEntity) {
            final C02PacketUseEntity packet = event.getPacket();
            if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                mc.thePlayer.setSprinting(false);
                PacketUtil.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                mc.thePlayer.setSprinting(true);
                PacketUtil.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
            }
        }
    });

}
