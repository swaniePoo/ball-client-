package me.dinozoid.strife.ui.clickgui.theme.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.ui.clickgui.component.implementations.BooleanComponent;
import me.dinozoid.strife.ui.clickgui.component.implementations.ColorPickerComponent;
import me.dinozoid.strife.ui.clickgui.component.implementations.EnumComponent;
import me.dinozoid.strife.ui.clickgui.component.implementations.SliderComponent;
import me.dinozoid.strife.ui.clickgui.panel.implementations.CategoryPanel;
import me.dinozoid.strife.ui.clickgui.panel.implementations.ModulePanel;
import me.dinozoid.strife.ui.clickgui.panel.implementations.MultiSelectPanel;
import me.dinozoid.strife.ui.clickgui.theme.Theme;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.StringUtil;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class StrifeTheme implements Theme {

    private final CustomFontRenderer font = Client.INSTANCE.getFontRepository().currentFont().size(19);

    @Override
    public void drawCategory(CategoryPanel panel, float x, float y, float width, float height) {
        String name = StringUtil.upperSnakeCaseToPascal(panel.category().name());
        Gui.drawRect(x - 1, y - 1, x + width + 1, y + height, 0xff252525);
        font.drawStringWithShadow(name, x + 2, y + panel.height() / 2 - font.getHeight(name) / 2, -1);
    }

    @Override
    public void drawModule(ModulePanel panel, float x, float y, float width, float height) {
        Gui.drawRect(x, y, x + width, y + height, 0xff333333);
        if (panel.module().toggled()) {
            Gui.drawRect(x, y, x + width, y + height, 0xffb82525);
        }
        font.drawStringWithShadow(panel.module().name(), x + 2, y + panel.height() / 2 - font.getHeight(panel.module().name()) / 2 - 0.5f, -1);
    }

    @Override
    public void drawMulti(MultiSelectPanel panel, float x, float y, float width, float height) {
        if (panel.extended())
            Gui.drawRect(x, y + height, x + width, y + panel.totalHeight(), 0xff1c1c1c);
        else
            font.drawStringWithShadow("...", x + width - font.getWidth("..."), y + panel.height() / 2 - font.getHeight("...") / 2 - 0.5f, -1);
        font.drawStringWithShadow(panel.property().getLabel(), x + 2, y + panel.height() / 2 - font.getHeight(panel.property().getLabel()) / 2 - 0.5f, -1);
    }

    @Override
    public void drawBindComponent(Module module, float x, float y, float width, float height, boolean focused) {
        String text = "Bind: [" + (focused ? " " : Keyboard.getKeyName(module.key())) + "]";
        font.drawStringWithShadow(text, x + 2, y + height / 2 - font.getHeight(text) / 2, -1);
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
        font.drawStringWithShadow(label.replaceAll("_", " ") + ": " + component.setting().getValue().toString().replaceAll("_", " "), x + 2, y + component.height() / 2 - font.getHeight(label) / 2 - 0.5f, -1);
    }

    @Override
    public void drawSliderComponent(SliderComponent component, float x, float y, float width, float height, float length) {
        Gui.drawRect(x, y, x + length, y + height, new Color(160, 36, 36).getRGB());
        String rep = "";
        switch (component.setting().representation()) {
            case INT:
                rep = "";
            case DOUBLE:
                rep = "";
                break;
            case DISTANCE:
                rep = "m/s";
                break;
            case PERCENTAGE:
                rep = "%";
                break;
            case MILLISECONDS:
                rep = "ms";
                break;
        }
        font.drawStringWithShadow(component.setting().getLabel() + ": " + (component.setting().representation() == Property.Representation.INT ? component.setting().getValue().intValue() : component.setting().getValue()) + rep, x + 2, y + height / 2 - font.getHeight(component.setting().getLabel()) / 2, -1);
    }

    @Override
    public void drawColorPickerComponent(ColorPickerComponent component, float x, float y, float width, float height) {
        Color color = component.setting().getValue();
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hue = hsb[0];
        RenderUtil.drawRect(x, y, x + width, y + height, Color.getHSBColor(hsb[0], 1, 1).getRGB());
        int brightnessMin = RenderUtil.toColorRGB(Color.HSBtoRGB(hue, 0, 1), 0).getRGB();
        int brightnessMax = RenderUtil.toColorRGB(Color.HSBtoRGB(hue, 0, 1), 255).getRGB();
        int saturationMin = RenderUtil.toColorRGB(Color.HSBtoRGB(hue, 1, 0), 0).getRGB();
        int saturationMax = RenderUtil.toColorRGB(Color.HSBtoRGB(hue, 1, 0), 255).getRGB();
        RenderUtil.drawGradientRect(x, y, x + width, y + height, brightnessMin, brightnessMax, true);
        Gui.drawGradientRect(x, y, x + width, y + height, saturationMin, saturationMax);
    }

}
