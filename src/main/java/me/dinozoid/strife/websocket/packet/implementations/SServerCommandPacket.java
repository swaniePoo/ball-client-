package me.dinozoid.strife.websocket.packet.implementations;

import com.google.gson.JsonElement;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;
import me.dinozoid.strife.websocket.user.User;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SServerCommandPacket extends Packet {

    public SServerCommandPacket() {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(SServerCommandPacket.class));
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {
        packetHandler.processServerCommandPacket(this);
    }

    public Object response() {
        switch (operation()) {
            case PACKET: {
                return Client.INSTANCE.getSocketClient().gson().fromJson(new String(Base64.getDecoder().decode(data.get("response").getAsString())), Packet.class);
            }
            case LIST_USERS: {
                System.out.println(data.get("response"));
                List<User> clientUsers = new ArrayList<>();
                for (JsonElement user : data.get("response").getAsJsonArray()) {
                    User u = null;
                    clientUsers.add(u = Client.INSTANCE.getSocketClient().gson().fromJson(user.getAsString(), User.class));
                    System.out.println(u.accountUsername());
                }
                return clientUsers;
            }
            default: {
                return data.get("response").getAsString();
            }
        }
    }

    public CServerCommandPacket.CommandOperation operation() {
        return CServerCommandPacket.CommandOperation.valueOf(data.get("operation").getAsString());
    }

    public String tag() {
        return data.get("tag").getAsString();
    }

}
