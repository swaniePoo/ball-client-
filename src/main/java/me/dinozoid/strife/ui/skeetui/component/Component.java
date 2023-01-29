package me.dinozoid.strife.ui.skeetui.component;

import me.dinozoid.strife.ui.skeetui.StrifeSkeetUI;
import me.dinozoid.strife.ui.skeetui.theme.Theme;
import me.dinozoid.strife.util.render.RenderUtil;

public abstract class Component {

    protected float x, y, width, height;
    protected boolean visible, focused;
    protected StrifeSkeetUI instance;
    protected Theme theme;

    public Component(float x, float y, float width, float height) {
        this(x, y, width, height, true);
    }

    public Component(float x, float y, float width, float height, boolean visible) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = visible;
        instance = StrifeSkeetUI.getInstance();
        theme = instance.getTheme();
    }

    public void init() {}
    public abstract void reset();

    public abstract void drawScreen(int mouseX, int mouseY);
    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);
    public abstract void mouseReleased(int mouseX, int mouseY, int state);
    public abstract void keyTyped(char typedChar, int keyCode);

    public boolean isHovered(int mouseX, int mouseY) {
        return RenderUtil.isHovered(x, y, width, height, mouseX, mouseY);
    }

    public void setPosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }
}
