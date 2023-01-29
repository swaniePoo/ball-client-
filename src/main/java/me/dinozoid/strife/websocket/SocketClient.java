package me.dinozoid.strife.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.websocket.packet.ClientPacketDeserializer;
import me.dinozoid.strife.websocket.packet.ClientPacketHandler;
import me.dinozoid.strife.websocket.packet.Packet;
import me.dinozoid.strife.websocket.packet.PacketEncoder;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class SocketClient extends WebSocketClient {

    private ClientPacketHandler packetHandler;
    private Gson gson;

    public SocketClient(URI serverUri) {
        super(serverUri);
        setTcpNoDelay(true);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        packetHandler = new ClientPacketHandler(this);
        packetHandler.init();
        gson = new GsonBuilder().registerTypeAdapter(Packet.class, new ClientPacketDeserializer<>(packetHandler)).create();
    }

    @Override
    public void onMessage(String message) {
        Packet packet = gson.fromJson(PacketEncoder.decode(message), Packet.class);
        if (packet != null) {
            JsonObject data = packet.data();
            packet.process(packetHandler);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Client.INSTANCE.connectSocket(false);
    }

    @Override
    public void onClosing(int code, String reason, boolean remote) {
        super.onClosing(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {

    }

    public Gson gson() {
        return gson;
    }

    public ClientPacketHandler packetHandler() {
        return packetHandler;
    }
}
