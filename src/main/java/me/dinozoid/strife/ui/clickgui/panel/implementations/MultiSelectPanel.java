package me.dinozoid.strife.ui.clickgui.panel.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.ui.clickgui.component.implementations.BooleanComponent;
import me.dinozoid.strife.util.system.StringUtil;

import java.util.Arrays;

public class MultiSelectPanel extends MultiComponentPanel {

    private final CustomFontRenderer font = Client.INSTANCE.getFontRepository().currentFont().size(19);
    private MultiSelectEnumProperty property;

    public MultiSelectPanel(MultiSelectEnumProperty property, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.property = property;
        Arrays.stream(property.constants()).forEach(constant -> {
            Property<Boolean> setting = new Property<>(StringUtil.upperSnakeCaseToPascal(constant.toString()), property.selected(constant));
            setting.addValueChange(((oldValue, value) -> {
                int index = 0;
                for (Enum constants : property.constants()) {
                    if (constants == constant)
                        property.value(index, value);
                    index++;
                }
            }));
            components.add(new BooleanComponent(setting, x, y, width, height));
        });
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        theme.drawMulti(this, x, y, width, height);
        super.drawScreen(mouseX, mouseY);
    }

    public MultiSelectEnumProperty property() {
        return property;
    }

    public void property(MultiSelectEnumProperty property) {
        this.property = property;
    }
}
