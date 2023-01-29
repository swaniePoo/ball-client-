package me.dinozoid.strife.websocket.packet.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;
import me.dinozoid.strife.websocket.user.User;

public class SChatPacket extends Packet {

    public SChatPacket() {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(SChatPacket.class));
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {
        packetHandler.processChatPacket(this);
    }

    public String message() {
        return data.get("msg").getAsString();
    }

    public User user() {
        return Client.INSTANCE.getSocketClient().gson().fromJson(data.get("user").getAsString(), User.class);
    }

}
