package me.dinozoid.strife.ui.csgoclickgui.panel;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.ui.csgoclickgui.IDrawableComponent;
import me.dinozoid.strife.util.render.RenderUtil;
import net.minecraft.client.gui.Gui;

public class ModulePanel implements IDrawableComponent {
    private final Module module;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private boolean hovered;
    private float animatedWidth;

    public ModulePanel(Module module, float x, float y, float width, float height) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hovered = RenderUtil.isHovered(x, y, width, height, mouseX, mouseY);
        animatedWidth = RenderUtil.animate(this.width, animatedWidth, 0.002f);
        Gui.drawRect(this.x, this.y, this.x + animatedWidth, this.y + this.height, this.module.toggled() ? 0xff121212 : 0xff0c0c0c);
        animatedWidth = hovered
                ? RenderUtil.animate(this.width, animatedWidth, 0.006f)
                : RenderUtil.animate(-this.width, animatedWidth, 0.006f) + 0.64f;
        Client.INSTANCE.getFontRepository().currentFont().size(19).drawStringWithShadow(this.module.name(), this.x + 2f, this.y + 5.4f, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && hovered) this.module.toggle();
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }
}
