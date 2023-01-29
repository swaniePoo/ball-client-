package me.dinozoid.strife.ui.skeetui;

import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.ui.skeetui.component.Component;
import me.dinozoid.strife.ui.skeetui.component.implementations.SidebarComponent;
import me.dinozoid.strife.ui.skeetui.theme.Theme;
import me.dinozoid.strife.ui.skeetui.theme.implementations.SkeetTheme;
import me.dinozoid.strife.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StrifeSkeetUI extends GuiScreen {

    private final List<Component> components = new ArrayList<>();
    private float x, y, origX, origY, dragX, dragY;
    private final int width = 480, height = 360;
    private static StrifeSkeetUI instance;
    private boolean dragging, closed;
    private final Theme theme;
    public long start;

    private final ResourceLocation background;

    public StrifeSkeetUI() {
        instance = this;
        theme = new SkeetTheme();
        background = new ResourceLocation("strife/gui/skeetui/background.png");
        components.add(new SidebarComponent(x, y, 35, height) {
            @Override
            public void init() {
                for (Category category : Category.values()) {
                    components.add(new SidebarButtonComponent(category, x, y, 35, 35));
                }
                super.init();
            }
        });
    }

    @Override
    public void initGui() {
        closed = false;
        start = System.currentTimeMillis();
    }

    public void updatePositions() {
        components.forEach(component -> component.setPosition(x, y, component.getWidth(), height));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int scaleFactor = scaledResolution.getScaleFactor();

        int width = this.width / scaleFactor;
        int height = this.height / scaleFactor;

        if (dragging) {
            x = mouseX + dragX;
            y = mouseY + dragY;
        }
        RenderUtil.drawImage(background, x, y, width, height);
        components.forEach(component -> component.setPosition(x, y, component.getWidth(), height));
        components.forEach(component -> {
            if (component.isVisible())
                component.drawScreen(mouseX, mouseY);
        });
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (RenderUtil.isHovered(x, y, width, height, mouseX, mouseY)) {
            if (mouseButton == 0) {
                dragging = true;
                dragX = (x - mouseX);
                dragY = (y - mouseY);
            }
        }
        components.forEach(component -> {
            if (component.isVisible() && component.isHovered(mouseX, mouseY))
                component.mouseClicked(mouseX, mouseY, mouseButton);
        });
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        components.forEach(component -> {
            if (component.isVisible())
                component.mouseReleased(mouseX, mouseY, state);
        });
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        components.forEach(component -> {
            if (component.isVisible())
                component.keyTyped(typedChar, keyCode);
        });
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        closed = true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static StrifeSkeetUI getInstance() {
        return instance;
    }

    public Theme getTheme() {
        return theme;
    }
}
