package me.dinozoid.strife.websocket.packet.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;

public class CChatPacket extends Packet {

    public CChatPacket(String message) {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(CChatPacket.class));
        data.addProperty("msg", message);
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {

    }

    public String message() {
        return data.get("msg").getAsString();
    }

}
