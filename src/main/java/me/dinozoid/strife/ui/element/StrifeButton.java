package me.dinozoid.strife.ui.element;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.ui.callback.ClickCallback;
import me.dinozoid.strife.util.render.RenderUtil;

import java.awt.*;

public class StrifeButton {

    private final CustomFontRenderer fontRenderer;
    private final String text;
    private final int color;
    private Position position;

    public StrifeButton(float x, float y, float width, float height, int fontSize, int color) {
        this(x, y, width, height, "", fontSize, color);
    }

    public StrifeButton(Position position, int fontSize, int color) {
        this(position.x(), position.y(), position.width(), position.height(), "", fontSize, color);
    }

    public StrifeButton(Position position, String text, int fontSize, int color) {
        this(position.x(), position.y(), position.width(), position.height(), text, fontSize, color);
    }

    public StrifeButton(float x, float y, float width, float height, String text, int fontSize, int color) {
        this.position = new Position(x, y, width, height);
        this.text = text;
        this.color = color;
        this.fontRenderer = Client.INSTANCE.getFontRepository().defaultFont().size(fontSize);
    }

    public void drawScreen(int mouseX, int mouseY) {
        float x = position.x();
        float y = position.y();
        float width = position.width();
        float height = position.height();
        if (RenderUtil.isHovered(x, y, width, height, mouseX, mouseY)) {
            RenderUtil.drawRoundedRect(x, y, x + width, y + height, 3, 15, RenderUtil.brighter(new Color(color), 0.85F).getRGB());
        } else {
            RenderUtil.drawRoundedRect(x, y, x + width, y + height, 3, 15, color);
        }
        float textX = position.x() + position.width() / 2 - fontRenderer.getWidth(text) / 2;
        float textY = position.y() + position.height() / 2 - fontRenderer.getHeight(text) / 2;
        fontRenderer.drawStringWithShadow(text, textX, textY, -1);
    }

    public void setPosition(float x, float y, float width, float height) {
        position = new Position(x, y, width, height);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton, ClickCallback callback) {
        if (RenderUtil.isHovered(position.x(), position.y(), position.width(), position.height(), mouseX, mouseY)) {
            callback.onClicked(mouseButton);
        }
    }

    public Position position() {
        return position;
    }

    public String text() {
        return text;
    }

    public int color() {
        return color;
    }
}