package me.dinozoid.strife.event.implementations.network;

import lombok.Setter;
import me.dinozoid.strife.event.Event;
import net.minecraft.network.Packet;

@Setter
public class PacketInboundEvent extends Event {
    private Packet packet;

    public PacketInboundEvent(Packet packet) {
        this.packet = packet;
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> T getPacket() {
        return (T) packet;
    }
}
