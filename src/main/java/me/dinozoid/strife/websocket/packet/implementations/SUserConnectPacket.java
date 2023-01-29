package me.dinozoid.strife.websocket.packet.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;
import me.dinozoid.strife.websocket.user.User;

public class SUserConnectPacket extends Packet {

    public SUserConnectPacket() {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(SUserConnectPacket.class));
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {
        packetHandler.processUserConnectPacket(this);
    }

    public User user() {
        return Client.INSTANCE.getSocketClient().gson().fromJson(data.get("user").getAsString(), User.class);
    }

}
