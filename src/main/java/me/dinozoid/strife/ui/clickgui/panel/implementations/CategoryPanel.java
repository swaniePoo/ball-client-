package me.dinozoid.strife.ui.clickgui.panel.implementations;

import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.ui.clickgui.panel.Panel;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.MathUtil;
import net.minecraft.util.MathHelper;

public class CategoryPanel extends Panel {

    private float dragX, dragY;
    private Category category;
    private boolean dragging;

    public CategoryPanel(Category category, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.category = category;
    }

    @Override
    public void reset() {
        origHeight = height;
        super.reset();
    }

    @Override
    public boolean visible() {
        return visible || Math.round(origHeight) != Math.round(totalHeight());
    }

    @Override
    public boolean extended() {
        return extended || Math.round(origHeight) != Math.round(totalHeight());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX + dragX;
            y = mouseY + dragY;
            origX = x;
            origY = y;
        }
        RenderUtil.makeCropBox(x, y, x + width, y + origHeight);
        origHeight = RenderUtil.animate(totalHeight(), origHeight, 0.05f);
        if (origHeight < 0) origHeight = 0;
        theme.drawCategory(this, x, y, width, Math.round(origHeight));
        super.drawScreen(mouseX, mouseY);
        RenderUtil.destroyCropBox();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovered(x, y, width, height, mouseX, mouseY)) {
            if (mouseButton == 0) {
                dragging = true;
                dragX = (x - mouseX);
                dragY = (y - mouseY);
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        dragging = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
    }

    public Category category() {
        return category;
    }

    public void category(Category category) {
        this.category = category;
    }
}
