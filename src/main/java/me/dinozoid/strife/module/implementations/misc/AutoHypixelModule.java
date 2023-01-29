package me.dinozoid.strife.module.implementations.misc;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.ui.notification.Notification;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.network.ServerUtil;
import me.dinozoid.strife.util.player.ChatFormatting;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.concurrent.TimeUnit;

@ModuleInfo(name = "AutoHypixel", renderName = "AutoHypixel", category = Category.MISC)
public class AutoHypixelModule extends Module {

    private final Property<Boolean> autoplayProperty = new Property<>("AutoPlay", true);
    private final Property<Boolean> autoGGProperty = new Property<>("AutoGG", true);
    private final EnumProperty<AutoPlayMode> autoPlayModeProperty = new EnumProperty<>("Play Mode", AutoPlayMode.SOLO_INSANE);
    private final DoubleProperty autoPlayDelayProperty = new DoubleProperty("Play Delay", 1, 0, 10, 1, Property.Representation.INT, () -> autoplayProperty.getValue());

    @EventHandler
    private final Listener<PacketInboundEvent> packetInboundListener = new Listener<>(event -> {
        if (event.getPacket() instanceof S02PacketChat) {
            final S02PacketChat chat = event.getPacket();
            if (mc.thePlayer.ticksExisted > 5 && ServerUtil.onServer("Hypixel")) {
                String message = ChatFormatting.stripFormatting(chat.getChatComponent().getFormattedText());
                if (message.contains("You won!") || message.contains("coins! (Win)") || message.contains("You died!")) {
                    if (autoGGProperty.getValue() && !message.contains("You died!"))
                        PacketUtil.sendPacketDelayedNoEvent(new C01PacketChatMessage("gg"), 1000);
                    if (autoplayProperty.getValue()) {
                        long delay = autoPlayDelayProperty.getValue().longValue();
                        Client.INSTANCE.getNotificationRepository().display(new Notification(Notification.NotificationType.SUCCESS, "AutoPlay", "Joining a new game in " + delay + (delay > 1 ? " seconds." : " second."), delay * 1000));
                        Client.INSTANCE.getScheduledExecutorService().schedule(() -> PacketUtil.sendPacketNoEvent(new C01PacketChatMessage("/play " + autoPlayModeProperty.getValue().toString().toLowerCase())), delay, TimeUnit.SECONDS);
                    }
                }
            }
        }
    });

    public enum AutoPlayMode {
        SOLO_NORMAL, SOLO_INSANE, TEAMS_NORMAL, TEAMS_INSANE
    }

}
