package me.dinozoid.strife.websocket.packet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.util.MinecraftUtil;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.websocket.SocketClient;
import me.dinozoid.strife.websocket.packet.implementations.*;
import me.dinozoid.strife.websocket.user.User;
import net.minecraft.network.play.client.C03PacketPlayer;

import java.util.List;
import java.util.Map;

public class ClientPacketHandler extends MinecraftUtil {

    private static final BiMap<Class<? extends Packet>, Integer> PACKETS = HashBiMap.create();

    private final SocketClient socketClient;

    public ClientPacketHandler(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void init() {
        PACKETS.put(SUserConnectPacket.class, 0);
        PACKETS.put(SChatPacket.class, 1);
        PACKETS.put(CChatPacket.class, 2);
        PACKETS.put(CBanStatisticPacket.class, 3);
        PACKETS.put(SSoundPacket.class, 4);
        PACKETS.put(STitlePacket.class, 5);
        PACKETS.put(SRetardFuckerPacket.class, 6);
        PACKETS.put(SUserUpdatePacket.class, 7);
        PACKETS.put(CUserUpdatePacket.class, 8);
        PACKETS.put(CServerCommandPacket.class, 9);
        PACKETS.put(SServerCommandPacket.class, 10);
    }

    public void sendPacket(Packet packet) {
        socketClient.send(PacketEncoder.encode(socketClient.gson().toJson(packet)));
    }

    public void processChatPacket(SChatPacket chatPacket) {
        if (mc.currentScreen == null) {
            PlayerUtil.sendMessage(PlayerUtil.ircChatColor(chatPacket.user()) + chatPacket.user().clientUsername() + "&f: " + chatPacket.message());
        } else {
            System.out.println(PlayerUtil.ircChatColor(chatPacket.user()) + chatPacket.user().clientUsername() + "&f: " + chatPacket.message());
        }
    }

    public void processRetardFuckerPacket(SRetardFuckerPacket fuckerPacket) {
        for (int i = 0; i < 12; i++) {
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + fuckerPacket.value(), mc.thePlayer.posZ, true));
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
        }
    }

    public void processTitlePacket(STitlePacket titlePacket) {
        mc.ingameGUI.displayTitle("", "", -1, -1, -1);
        mc.ingameGUI.setDefaultTitlesTimes();
        mc.ingameGUI.displayTitle(titlePacket.title(), null, titlePacket.fadeIn(), titlePacket.stay(), titlePacket.fadeOut());
        mc.ingameGUI.displayTitle(null, titlePacket.subtitle(), titlePacket.fadeIn(), titlePacket.stay(), titlePacket.fadeOut());
    }

    public void processUserConnectPacket(SUserConnectPacket userPacket) {
        Client.INSTANCE.setStrifeUser(userPacket.user());
    }

    public void processServerCommandPacket(SServerCommandPacket commandPacket) {
        switch (commandPacket.operation()) {
            case LIST_USERS: {
                List<User> users = (List<User>) commandPacket.response();
                Client.INSTANCE.setOnlineUsers(users);
                if(commandPacket.tag().equalsIgnoreCase("irc")) {
                    PlayerUtil.sendMessage(" ");
                    PlayerUtil.sendMessage("&7All online users. (&c" + users.size() + "&7)");
                    StringBuilder stringBuilder = new StringBuilder();
                    for (User user : users)
                        stringBuilder.append(users.indexOf(user) > 0 ? "&7, " : "").append(PlayerUtil.ircChatColor(user)).append(user.clientUsername());
                    PlayerUtil.sendMessage(stringBuilder.toString());
                    PlayerUtil.sendMessage(" ");
                }
                break;
            }
        }
    }

    public Class<? extends Packet> getPacketByID(int id) {
        return PACKETS.inverse().get(id);
    }

    public int getIDForPacket(Class<? extends Packet> packet) {
        return PACKETS.get(packet);
    }

    public void processUserUpdatePacket(SUserUpdatePacket userUpdatePacket) {
        for (Map.Entry<String, JsonElement> entry : userUpdatePacket.values().entrySet()) {
            SUserUpdatePacket.UpdateType type = SUserUpdatePacket.UpdateType.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());
            User user = Client.INSTANCE.getStrifeUser();
            switch (type) {
                case UID: {
                    user.uid(value);
                    break;
                }
                case CLIENT_USERNAME: {
                    user.clientUsername(value);
                    break;
                }
                case RANK: {
                    user.rank(value);
                    break;
                }
                case ACCOUNT_USERNAME: {
                    user.accountUsername(value);
                    break;
                }
            }
        }
    }

}
