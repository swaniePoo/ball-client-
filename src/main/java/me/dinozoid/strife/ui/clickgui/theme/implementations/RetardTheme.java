package me.dinozoid.strife.ui.clickgui.theme.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.ui.clickgui.component.implementations.BooleanComponent;
import me.dinozoid.strife.ui.clickgui.component.implementations.ColorPickerComponent;
import me.dinozoid.strife.ui.clickgui.component.implementations.EnumComponent;
import me.dinozoid.strife.ui.clickgui.component.implementations.SliderComponent;
import me.dinozoid.strife.ui.clickgui.panel.implementations.CategoryPanel;
import me.dinozoid.strife.ui.clickgui.panel.implementations.ModulePanel;
import me.dinozoid.strife.ui.clickgui.panel.implementations.MultiSelectPanel;
import me.dinozoid.strife.ui.clickgui.theme.Theme;
import me.dinozoid.strife.util.system.StringUtil;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class RetardTheme implements Theme {

    private final CustomFontRenderer font = Client.INSTANCE.getFontRepository().currentFont().size(19);

    @Override
    public void drawCategory(CategoryPanel panel, float x, float y, float width, float height) {
        String name = StringUtil.upperSnakeCaseToPascal(panel.category().name());
        Gui.drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xff252525);
        font.drawStringWithShadow(name, x + 2, y + panel.height() / 2 - font.getHeight(name) / 2, -1);
    }

    @Override
    public void drawModule(ModulePanel panel, float x, float y, float width, float height) {
        Gui.drawRect(x, y, x + width, y + height, 0xff40635e);
        if (panel.module().toggled()) {
            Gui.drawRect(x, y, x + width, y + height, 0xff00ffd9);
        }
        font.drawStringWithShadow(panel.module().name(), (float) (x + 2 + Math.random() * 5f), (float) (y + panel.height() / 2 - font.getHeight(panel.module().name()) / 2 + Math.random() * 5f), -1);
    }

    @Override
    public void drawMulti(MultiSelectPanel panel, float x, float y, float width, float height) {

    }

    @Override
    public void drawBindComponent(Module module, float x, float y, float width, float height, boolean focused) {

    }

    @Override
    public void drawBooleanComponent(BooleanComponent component, float x, float y, float width, float height, float settingWidth, float settingHeight, int opacity) {
        String label = component.setting().getLabel();
        font.drawStringWithShadow(label, x + 2, y + component.height() / 2 - font.getHeight(label) / 2, -1);
        Gui.drawRect(x + width - settingWidth - 1, y + height / 2 - settingHeight / 2, x + width - 1, y + height / 2 - settingHeight / 2 + settingHeight, 0xff1f1f1f);
        if (component.setting().getValue()) {
            settingWidth -= 1;
            settingHeight -= 1;
            Gui.drawRect(x + width - settingWidth - 1.2f, y + height / 2 - settingHeight / 2 + 0.2f, x + width - 1.2f, y + height / 2 - settingHeight / 2 + settingHeight - 0.3f, new Color(184, 37, 37, opacity).getRGB());
        }
    }

    @Override
    public void drawEnumComponent(EnumComponent component, float x, float y, float width, float height) {
        String label = component.setting().getLabel();
        font.drawStringWithShadow(label + ": " + component.setting().getValue(), x + 2, y + component.height() / 2 - font.getHeight(label) / 2, -1);
    }

    @Override
    public void drawSliderComponent(SliderComponent component, float x, float y, float width, float height, float length) {

    }

    @Override
    public void drawColorPickerComponent(ColorPickerComponent component, float x, float y, float width, float height) {

    }

}
