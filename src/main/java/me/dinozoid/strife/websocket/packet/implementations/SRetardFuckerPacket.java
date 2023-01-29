package me.dinozoid.strife.websocket.packet.implementations;


import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;

public class SRetardFuckerPacket extends Packet {

    public SRetardFuckerPacket() {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(SRetardFuckerPacket.class));
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {
        packetHandler.processRetardFuckerPacket(this);
    }

    public float value() {
        return data.get("value").getAsFloat();
    }

}
