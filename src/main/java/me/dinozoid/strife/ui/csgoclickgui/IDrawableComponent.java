package me.dinozoid.strife.ui.csgoclickgui;

public interface IDrawableComponent {
    void drawScreen(int mouseX, int mouseY, float partialTicks);

    void mouseClicked(int mouseX, int mouseY, int mouseButton);

    void mouseReleased(int mouseX, int mouseY, int state);

    void keyTyped(char typedChar, int keyCode);
}
