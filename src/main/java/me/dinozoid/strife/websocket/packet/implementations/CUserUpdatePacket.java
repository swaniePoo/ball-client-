package me.dinozoid.strife.websocket.packet.implementations;

import com.google.gson.JsonObject;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;

import java.util.Arrays;

public class CUserUpdatePacket extends Packet {

    public CUserUpdatePacket(SUserUpdatePacket.Value... values) {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(CUserUpdatePacket.class));
        JsonObject object = new JsonObject();
        Arrays.stream(values).forEach(value -> object.addProperty(String.valueOf(value.type()), String.valueOf(value.value())));
        data.add("values", object);
    }


    @Override
    public void process(ClientPacketHandler packetHandler) {

    }

    public JsonObject values() {
        return data.getAsJsonObject("values");
    }

}
