package me.dinozoid.strife.websocket.packet.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;

import java.util.Base64;

public class CServerCommandPacket extends Packet {

    public CServerCommandPacket(final CommandOperation commandOperation, Object request, String tag) {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(CServerCommandPacket.class));
        data.addProperty("operation", String.valueOf(commandOperation));
        data.addProperty("tag", tag);
        switch (commandOperation) {
            case LIST_USERS:
            case DISCONNECT_USER:
            case UNMUTE_USER:
            case MUTE_USER: {
                data.addProperty("request", String.valueOf(request));
                break;
            }
            case PACKET: {
                data.addProperty("request", Base64.getEncoder().encodeToString(Client.INSTANCE.getSocketClient().gson().toJson(request, Packet.class).getBytes()));
                break;
            }
        }
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {

    }

    public Object request() {
        switch (CommandOperation.valueOf(data.get("operation").getAsString())) {
            case PACKET: {
                return Client.INSTANCE.getSocketClient().gson().fromJson(new String(Base64.getDecoder().decode(data.get("request").getAsString())), Packet.class);
            }
            default: {
                return data.get("request").getAsString();
            }
        }
    }

    public CommandOperation operation() {
        return CommandOperation.valueOf(data.get("operation").getAsString());
    }
    public String tag() {
        return data.get("tag").getAsString();
    }

    public enum CommandOperation {
        MUTE_USER, UNMUTE_USER, DISCONNECT_USER, LIST_USERS, PACKET
    }

}
