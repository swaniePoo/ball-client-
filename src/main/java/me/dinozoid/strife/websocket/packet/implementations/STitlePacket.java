package me.dinozoid.strife.websocket.packet.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;

public class STitlePacket extends Packet {

    public STitlePacket() {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(STitlePacket.class));
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {
        packetHandler.processTitlePacket(this);
    }

    public String title() {
        return data.get("title").getAsString();
    }

    public String subtitle() {
        return data.get("subtitle").getAsString();
    }

    public int fadeIn() {
        return data.get("fade").getAsInt();
    }

    public int stay() {
        return data.get("stay").getAsInt();
    }

    public int fadeOut() {
        return data.get("fadeout").getAsInt();
    }

}
