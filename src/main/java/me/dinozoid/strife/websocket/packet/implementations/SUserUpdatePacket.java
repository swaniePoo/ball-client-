package me.dinozoid.strife.websocket.packet.implementations;

import com.google.gson.JsonObject;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;

public class SUserUpdatePacket extends Packet {

    public SUserUpdatePacket() {
        super(Client.INSTANCE.getSocketClient().packetHandler().getIDForPacket(SUserUpdatePacket.class));
    }

    @Override
    public void process(ClientPacketHandler packetHandler) {
        packetHandler.processUserUpdatePacket(this);
    }

    public JsonObject values() {
        return data.getAsJsonObject("values");
    }

    public enum UpdateType {
        ACCOUNT_USERNAME, CLIENT_USERNAME, UID, RANK
    }

    public static class Value {
        private final UpdateType type;
        private final Object value;

        public Value(final UpdateType type, final Object value) {
            this.type = type;
            this.value = value;
        }

        public UpdateType type() {
            return type;
        }

        public Object value() {
            return value;
        }
    }

}
