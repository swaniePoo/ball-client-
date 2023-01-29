package me.dinozoid.strife.websocket.packet.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;

public class CBanStatisticPacket extends Packet {

    public CBanStatisticPacket(String reason, long time) {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(CBanStatisticPacket.class));
        data.addProperty("reason", reason);
        data.addProperty("time", time);
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {

    }

    public long time() {
        return data.get("time").getAsLong();
    }

    public String reason() {
        return data.get("reason").getAsString();
    }
}
