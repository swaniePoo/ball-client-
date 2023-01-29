package me.dinozoid.strife.ui.notification;

import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class Notification {

    private NotificationType type;
    private String title, subtitle;
    private boolean visible;
    private long start, duration;

    private float width, height;
    private float animatedWidth, animatedHeight;

    private final TimerUtil timerUtil = new TimerUtil();

    public Notification(final NotificationType type, final String title, final String subtitle) {
        this(type, title, subtitle, 3000);
    }

    public Notification(final NotificationType type, final String title, final String subtitle, final long duration) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.duration = duration;
        this.visible = true;
        this.height = 27f;
    }

    public void render(final CustomFontRenderer font19, final CustomFontRenderer font21, final float x, float y, final float yOff) {
        if (start == 0) {
            start = System.currentTimeMillis();
            this.width = Math.max(font21.getWidth(title), font19.getWidth(subtitle)) + 30;
        } else if (System.currentTimeMillis() - start > duration) {
            visible = false;
        }
        y -= yOff;
        if (visible) {
            if (timerUtil.hasElapsed(12)) {
                animatedWidth = RenderUtil.animate(width, animatedWidth, 0.08f) - 0.01f;
                animatedHeight = RenderUtil.animate(height, animatedHeight, 0.08f) - 0.01f;
                timerUtil.reset();
            }
        } else {
            if (timerUtil.hasElapsed(12)) {
                animatedWidth = RenderUtil.animate(-width, animatedWidth, 0.08f) - 0.01f;
                animatedHeight = RenderUtil.animate(0, animatedHeight, 0.08f) - 0.01f;
                timerUtil.reset();
            }
        }
        float progress = ((System.currentTimeMillis() - start) / (float) duration) * width;
        float xPos = x - animatedWidth;
        RenderUtil.drawRect(xPos, y, x, y + animatedHeight, -1879048192);
        RenderUtil.drawRect(xPos, y + height - 1, xPos + animatedWidth, y + height, type.color.darker().darker().getRGB());
        RenderUtil.drawRect(xPos, y + height - 1, xPos + progress, y + height, type.color.getRGB());
        RenderUtil.drawImage(new ResourceLocation(type.path), xPos + 4, y + 6, 15, 15);
        font21.drawStringWithShadow(title, xPos + 20 + 2, y + 3, type.color.getRGB());
        font19.drawStringWithShadow(subtitle, xPos + 20 + 2.1f, y + font21.getHeight(title) + 2, -1);
    }

    public boolean visible() {
        return visible;
    }

    public void visible(boolean visible) {
        this.visible = visible;
    }

    public long start() {
        return start;
    }

    public void start(long start) {
        this.start = start;
    }

    public NotificationType type() {
        return type;
    }

    public void type(final NotificationType type) {
        this.type = type;
    }

    public String title() {
        return title;
    }

    public void title(final String title) {
        this.title = title;
    }

    public String subtitle() {
        return subtitle;
    }

    public void subtitle(final String subtitle) {
        this.subtitle = subtitle;
    }

    public long duration() {
        return duration;
    }

    public void duration(final long duration) {
        this.duration = duration;
    }

    public float width() {
        return width;
    }

    public void width(final float width) {
        this.width = width;
    }

    public float height() {
        return height;
    }

    public void height(float height) {
        this.height = height;
    }

    public float animatedWidth() {
        return animatedWidth;
    }

    public void animatedWidth(final float animatedWidth) {
        this.animatedWidth = animatedWidth;
    }

    public float animatedHeight() {
        return animatedHeight;
    }

    public void animatedHeight(final float animatedHeight) {
        this.animatedHeight = animatedHeight;
    }

    public enum NotificationType {

        SUCCESS("strife/gui/notification/success.png", new Color(123, 209, 10)), WARNING("strife/gui/notification/warning.png", new Color(237, 214, 43)), ERROR("strife/gui/notification/error.png", new Color(224, 70, 70)), INFO("strife/gui/notification/info.png", new Color(255, 255, 255));

        private final String path;
        private final Color color;

        NotificationType(String path, Color color) {
            this.path = path;
            this.color = color;
        }

        public String path() {
            return path;
        }

        public Color color() {
            return color;
        }
    }
}
