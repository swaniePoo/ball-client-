package me.dinozoid.strife.ui.clickgui.theme;

import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.ui.clickgui.component.implementations.BooleanComponent;
import me.dinozoid.strife.ui.clickgui.component.implementations.ColorPickerComponent;
import me.dinozoid.strife.ui.clickgui.component.implementations.EnumComponent;
import me.dinozoid.strife.ui.clickgui.component.implementations.SliderComponent;
import me.dinozoid.strife.ui.clickgui.panel.implementations.CategoryPanel;
import me.dinozoid.strife.ui.clickgui.panel.implementations.ModulePanel;
import me.dinozoid.strife.ui.clickgui.panel.implementations.MultiSelectPanel;

public interface Theme {
    void drawCategory(CategoryPanel panel, float x, float y, float width, float height);
    void drawModule(ModulePanel panel, float x, float y, float width, float height);
    void drawMulti(MultiSelectPanel panel, float x, float y, float width, float height);
    void drawBindComponent(Module module, float x, float y, float width, float height, boolean focused);
    void drawBooleanComponent(BooleanComponent component, float x, float y, float width, float height, float settingWidth, float settingHeight, int opacity);
    void drawEnumComponent(EnumComponent component, float x, float y, float width, float height);
    void drawSliderComponent(SliderComponent component, float x, float y, float width, float height, float length);
    void drawColorPickerComponent(ColorPickerComponent component, float x, float y, float width, float height);
}
