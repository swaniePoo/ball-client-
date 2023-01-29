package me.dinozoid.strife.websocket.packet.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;

import java.util.Base64;

public class SSoundPacket extends Packet {

    public SSoundPacket() {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(SSoundPacket.class));
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {
        //packetHandler.processSendSoundPacket(this);
    }

    public byte[] bytes() {
        return Base64.getDecoder().decode(data.get("bytes").getAsString());
    }
}
