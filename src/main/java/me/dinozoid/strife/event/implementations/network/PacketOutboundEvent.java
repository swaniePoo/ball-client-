package me.dinozoid.strife.event.implementations.network;

import lombok.AllArgsConstructor;
import lombok.Setter;
import me.dinozoid.strife.event.Event;
import net.minecraft.network.Packet;

@Setter
@AllArgsConstructor
public class PacketOutboundEvent extends Event {

    private Packet packet;

    public <T extends Packet> T getPacket() {
        return (T) packet;
    }
}

