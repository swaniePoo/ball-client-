package me.dinozoid.strife.util.network;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listenable;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.util.MinecraftUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PacketUtil extends MinecraftUtil implements Listenable {

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(75);
    private static final List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();
    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        scheduledFutures.removeIf(Future::isDone);
        PlayerUtil.sendMessageWithPrefix("clear");
    });

    public static void sendPacketNoEvent(Packet<?> packet) {
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    public static void sendPacket(Packet<?> packet) {
        mc.getNetHandler().addToSendQueue(packet);
    }

    public static void sendPacketTimes(Packet<?> packet, int timesToSend) {
        for (int i = 0; i < timesToSend; i++) {
            sendPacket(packet);
        }
    }

    public static void packetTimesNoEvent(Packet<?> packet, int timesToSend) {
        for (int i = 0; i < timesToSend; i++) {
            sendPacketNoEvent(packet);
        }
    }

    public static void sendPacketDelayed(Packet<?> packet, long delay) {
        scheduledFutures.add(scheduledExecutorService.schedule(() -> sendPacket(packet), delay, TimeUnit.MILLISECONDS));
    }

    public static void sendPacketDelayedNoEvent(Packet<?> packet, long delay) {
        scheduledFutures.add(scheduledExecutorService.schedule(() -> sendPacketNoEvent(packet), delay, TimeUnit.MILLISECONDS));
    }

}
