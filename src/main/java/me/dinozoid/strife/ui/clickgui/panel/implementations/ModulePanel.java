package me.dinozoid.strife.ui.clickgui.panel.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.ui.clickgui.panel.Panel;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.TimerUtil;

public class ModulePanel extends Panel {

    private final TimerUtil timer = new TimerUtil();
    private final CustomFontRenderer font = Client.INSTANCE.getFontRepository().currentFont().size(17);
    private Module module;

    public ModulePanel(Module module, float x, float y, float width, float height) {
        super(x, y, width, height, false);
        this.module = module;
    }

    @Override
    public boolean visible() {
        return visible;
    }

    @Override
    public boolean extended() {
        return extended;
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        origHeight = RenderUtil.animate(totalHeight(), origHeight, 0.05f);
        if (origHeight < 0) origHeight = 0;
        theme.drawModule(this, x, y, width, height);
//        String description = "No description.";
//        ScaledResolution sc = new ScaledResolution(Minecraft.getMinecraft());
//        if (!module.description().isEmpty())
//            description = module.description();
//        if (isHovered(mouseX, mouseY))
//            font.drawStringWithShadow(description, sc.getScaledWidth() - font.getWidth(description), sc.getScaledHeight() - font.getHeight(description), -1);
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
            if (mouseButton == 0) {
                module.toggled(!module.toggled());
                timer.reset();
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
    }

    public Module module() {
        return module;
    }

    public void module(Module module) {
        this.module = module;
    }

}
