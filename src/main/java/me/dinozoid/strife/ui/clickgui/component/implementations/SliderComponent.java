package me.dinozoid.strife.ui.clickgui.component.implementations;

import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.ui.clickgui.component.SettingComponent;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.MathUtil;

public class SliderComponent extends SettingComponent<DoubleProperty> {

    private boolean sliding;

    public SliderComponent(DoubleProperty setting, float x, float y, float width, float height) {
        super(setting, x, y, width, height);
    }

    public SliderComponent(DoubleProperty setting, float x, float y, float width, float height, boolean visible) {
        super(setting, x, y, width, height, visible);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        if (!visible) return;
        double deltaMaxMin = setting.max() - setting.min();
        double startX = x;
        double length = (setting.getValue() - setting.min()) / deltaMaxMin * width;
        if (sliding) {
            setting.setValue(MathUtil.round(setting.min() + (mouseX - startX) / width * deltaMaxMin, 2, setting.increment()));
        }
        theme.drawSliderComponent(this, (float)startX, y, width, height, (float)length);
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (visible && !sliding && mouseButton == 0 && RenderUtil.isHovered(x, y, width, height, mouseX, mouseY))
            sliding = true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        sliding = false;
        if (!visible) return;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }
}
