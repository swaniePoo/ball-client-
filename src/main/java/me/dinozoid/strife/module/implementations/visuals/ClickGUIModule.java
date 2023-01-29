package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;

@ModuleInfo(name = "ClickGUI", renderName = "ClickGUI", description = "Shows this menu.", category = Category.VISUALS)
public class ClickGUIModule extends Module {

    private final EnumProperty<ClickGUIMode> modeProperty = new EnumProperty<>("Mode", ClickGUIMode.DROPDOWN);
    private final Property<Boolean> blurProperty = new Property<>("Blur", true);
    private final DoubleProperty blurIntensityProperty = new DoubleProperty("Blur Intensity", 25, 1, 100, 1, Property.Representation.INT, () -> blurProperty.getValue());

    @Override
    public void onEnable() {
        super.onEnable();
        toggled(false);
    }

    public enum ClickGUIMode {
        DROPDOWN, SKEET
    }

    public static ClickGUIModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(ClickGUIModule.class);
    }

    public Property<Boolean> blurProperty() {
        return blurProperty;
    }
    public DoubleProperty blurIntensityProperty() {
        return blurIntensityProperty;
    }
    public EnumProperty<ClickGUIMode> modeProperty() {
        return modeProperty;
    }
}
