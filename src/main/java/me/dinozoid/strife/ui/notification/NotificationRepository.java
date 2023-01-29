package me.dinozoid.strife.ui.notification;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.util.MinecraftUtil;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository extends MinecraftUtil {

    private final List<Notification> NOTIFICATIONS = new ArrayList<>();
    private CustomFontRenderer font19, font21;

    public void init() {
        if(font19 == null)
            font19 = Client.INSTANCE.getFontRepository().currentFont().size(19);
        if(font21 == null)
            font21 = Client.INSTANCE.getFontRepository().currentFont().size(21);
    }

    public void drawNotifications() {
        float yOffset = 3f;
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        NOTIFICATIONS.removeIf(notification -> notification.animatedWidth() <= -notification.width() && !notification.visible());
        for (Notification notification : new ArrayList<>(NOTIFICATIONS)) {
            notification.render(font19, font21, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight() - notification.height(), yOffset);
            yOffset += notification.animatedHeight() + 4f;
        }
    }

    public void display(Notification notification) {
        NOTIFICATIONS.add(notification);
    }

}
