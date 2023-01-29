package me.dinozoid.strife.ui.clickgui.component.implementations;

import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.ui.clickgui.component.SettingComponent;

public class StringComponent extends SettingComponent<Property<String>> {

    public StringComponent(Property<String> setting, float x, float y, float width, float height) {
        super(setting, x, y, width, height);
    }

    public StringComponent(Property<String> setting, float x, float y, float width, float height, boolean visible) {
        super(setting, x, y, width, height, visible);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

}
