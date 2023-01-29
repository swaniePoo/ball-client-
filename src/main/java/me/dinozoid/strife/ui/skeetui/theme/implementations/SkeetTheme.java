package me.dinozoid.strife.ui.skeetui.theme.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.font.CustomFont;
import me.dinozoid.strife.ui.skeetui.component.implementations.SidebarComponent;
import me.dinozoid.strife.ui.skeetui.theme.Theme;
import me.dinozoid.strife.util.render.RenderUtil;

import java.awt.*;

public class SkeetTheme implements Theme {

    private final CustomFont csgoFont = Client.INSTANCE.getFontRepository().fontBy("BadCache");

    @Override
    public void drawSidebarComponent(int mouseX, int mouseY, SidebarComponent component) {
        RenderUtil.drawOutlinedRect(component.getX(), component.getY(), component.getX() + component.getWidth(), component.getY() + component.getHeight(), 1,0x00000000, new Color(43, 41, 39).getRGB());
    }

    @Override
    public void drawSidebarButtonComponent(int mouseX, int mouseY, SidebarComponent.SidebarButtonComponent component) {
        if(!component.isHovered(mouseX, mouseY))
            RenderUtil.drawOutlinedRect(component.getX(), component.getY(), component.getX() + component.getWidth(), component.getY() + component.getHeight(), 1,new Color(12, 12, 12).getRGB(), new Color(43, 41, 39).getRGB());
    }
}
