package me.dinozoid.strife.ui.csgoclickgui.panel;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.ui.csgoclickgui.IDrawableComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static me.dinozoid.strife.util.render.RenderUtil.*;

public class CategoryPanel implements IDrawableComponent {
    private final Category category;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private float animatedWidth;
    private final int animatedFontSize = 16;
    private boolean hovered, clickedAndReleased, expanded;
    private final List<ModulePanel> modules = new ArrayList<>();

    public CategoryPanel(Category category, float x, float y, float width, float height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        float xOff = x + 130f;
        float yOff = 4f;
        float moduleWidth = 160f;
        float moduleHeight = 20f;
        for (Module m : Client.INSTANCE.getModuleRepository().modulesIn(category)) {
            this.modules.add(new ModulePanel(m, xOff, yOff, moduleWidth, moduleHeight));

            yOff += 4f + moduleHeight;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xff232323);
//        animatedWidth = animate(this.width, animatedWidth, 0.002f);
        hovered = isHovered(x, y, width, height, mouseX, mouseY);
        drawRect(this.x, this.y, this.x + animatedWidth, this.y + this.height, 0xff861896);

        Client.INSTANCE.getFontRepository().currentFont().size(27).drawStringWithShadow(StringUtils.capitalize(this.category.name().toLowerCase(Locale.ROOT)), this.x + 4, this.y + 9.5f, -1);

        animatedWidth = hovered ? animate(this.width, animatedWidth, 0.02f)
                : animate(-this.width - 0.2, animatedWidth, 0.02f);

        if (expanded) this.modules.forEach(modulePanel -> modulePanel.drawScreen(mouseX, mouseY, partialTicks));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && hovered) clickedAndReleased = true;
        else if (mouseButton == 1 && hovered) expanded = !expanded;

        if (expanded) this.modules.forEach(modulePanel -> modulePanel.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) clickedAndReleased = false;

        if (expanded) this.modules.forEach(modulePanel -> modulePanel.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }
}
