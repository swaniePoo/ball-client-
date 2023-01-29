package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S02PacketChat;

@ModuleInfo(name = "NoFall", renderName = "NoFall", description = "Take no fall damage.", category = Category.MOVEMENT)
public class NoFallModule extends Module {

    private final EnumProperty<NoFallMode> noFallModeProperty = new EnumProperty<>("Mode", NoFallMode.PACKET);
    private LongJumpModule longJumpModule;
    private int value;

    @Override
    public void onEnable() {
        super.onEnable();
        if (longJumpModule == null)
            longJumpModule = Client.INSTANCE.getModuleRepository().moduleBy(LongJumpModule.class);
    }

    @EventHandler
    private final Listener<PacketInboundEvent> packetInboundEventListener = new Listener<>(event -> {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat s02 = event.getPacket();
            if (s02.getChatComponent().getUnformattedText().contains("Cages open")) {
                value = 100;
            }
        }
    });

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        if (FlightModule.instance().toggled()) return;
        switch (noFallModeProperty.getValue()) {
            case SPOOF:
                if (mc.thePlayer.fallDistance > 3) {
                    event.setGround(true);
                }
                break;
            case PACKET:
                if (mc.thePlayer.fallDistance > 3 && PlayerUtil.isBlockUnder()) {
                    PacketUtil.sendPacketNoEvent(new C03PacketPlayer(true));
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                    mc.thePlayer.fallDistance = 0;
                }
                break;
            case HYPIXEL:
                value--;
                if (mc.thePlayer.fallDistance >= 3 && !longJumpModule.toggled() && PlayerUtil.isBlockUnder() && value < 0) {
                    event.setPosY(event.getPosY() - (event.getPosX() % 1 / 64));
                    event.setGround(true);
                }
                break;
        }
    });

    @EventHandler
    private final Listener<PacketOutboundEvent> packetOutboundListener = new Listener<>(event -> {
        if (FlightModule.instance().toggled()) return;
        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer packet = event.getPacket();
            switch (noFallModeProperty.getValue()) {
                case NOGROUND: {
                    packet.setOnGround(false);
                    break;
                }
                case EDIT: {
                    packet.setOnGround(true);
                    mc.thePlayer.fallDistance = 0;
                    break;
                }
            }
        }
    });

    @Override
    public void init() {
        super.init();
        addValueChangeListener(noFallModeProperty);
    }

    private enum NoFallMode {
        SPOOF, NOGROUND, EDIT, PACKET, HYPIXEL
    }

}