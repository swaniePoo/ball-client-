package me.dinozoid.strife.ui.csgoclickgui;

import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.ui.csgoclickgui.panel.CategoryPanel;
import me.dinozoid.strife.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSGOClickGui extends GuiScreen {
    private final float categoryWidth = 110f, categoryHeight = 34f;
    private final float x;
    private final float y;
    private final List<CategoryPanel> panels = new ArrayList<>();

    public CSGOClickGui(float x, float y) {
        this.x = x;
        this.y = y;
        int yOffset = 24;
        for (Category category : Category.values()) {
            this.panels.add(new CategoryPanel(category, x, yOffset, categoryWidth, categoryHeight));
            yOffset += 6 + categoryHeight;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float width = 450f;
        float height = 220f;
        RenderUtil.drawRect(this.x, this.y + 15f, this.x + width, this.y + height, 0xff181818);
        RenderUtil.makeCropBox(this.x, this.y + 16f, this.x + width, this.y + height);
        this.panels.forEach(panel -> panel.drawScreen(mouseX, mouseY, partialTicks));
        RenderUtil.destroyCropBox();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.panels.forEach(panels -> panels.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.panels.forEach(panel -> panel.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.panels.forEach(panel -> panel.keyTyped(typedChar, keyCode));
        super.keyTyped(typedChar, keyCode);
    }
}
