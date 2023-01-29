package me.dinozoid.strife.util.network;

import me.dinozoid.strife.util.MinecraftUtil;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerData;

public class ServerUtil extends MinecraftUtil {

//    public static ServerData currentServer;

    public static boolean onServer(final String server) {
        if (mc == null || mc.isSingleplayer() || mc.getCurrentServerData() == null) return false;
        final ServerData serverData = mc.getCurrentServerData();
        final ServerData currentServer = GuiMultiplayer.currentServer();
        return !mc.isSingleplayer() && (serverData.serverIP.toLowerCase().contains(server.toLowerCase()) || currentServer != null && currentServer.serverIP.toLowerCase().contains(server.toLowerCase()));
    }

    public static boolean onServer() {
        return mc != null && !mc.isSingleplayer() && (mc.getCurrentServerData() != null || GuiMultiplayer.currentServer() != null);
    }

    public static String getCurrentIP() {
        String ip = "Singleplayer";
        ServerData currentServer = !mc.isSingleplayer() ? mc.getCurrentServerData() != null ? mc.getCurrentServerData() : GuiMultiplayer.currentServer() != null ? GuiMultiplayer.currentServer() : null : null;
        if (currentServer != null)
            ip = currentServer.serverIP;
        return ip;
    }

    public static ServerData getServerData() {
        return !mc.isSingleplayer() ? mc.getCurrentServerData() != null ? mc.getCurrentServerData() : GuiMultiplayer.currentServer() != null ? GuiMultiplayer.currentServer() : null : null;
    }
}