package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.implementations.ColorProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.render.RenderUtil;

import java.awt.*;

@ModuleInfo(name = "OutlineESP", renderName = "OutlineESP", aliases = "Outline", category = Category.VISUALS)
public class OutlineESPModule extends Module {

    private final MultiSelectEnumProperty<PlayerUtil.Target> targetsProperty = new MultiSelectEnumProperty<>("Targets", PlayerUtil.Target.PLAYERS);
    private final EnumProperty<RenderUtil.ColorMode> colorModeProperty = new EnumProperty<>("Color Mode", RenderUtil.ColorMode.ASTOLFO);
    private final ColorProperty colorProperty = new ColorProperty("Color", new Color(209, 50, 50), () -> colorModeProperty.getValue() == RenderUtil.ColorMode.STATIC || colorModeProperty.getValue() == RenderUtil.ColorMode.PULSE || colorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);
    private final ColorProperty secondColorProperty = new ColorProperty("Color", new Color(29, 205, 200), () -> colorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);

    public int getColor(int index) {
        return RenderUtil.getColor(colorModeProperty, index, colorProperty.getValue(), secondColorProperty.getValue());
    }

    public MultiSelectEnumProperty<PlayerUtil.Target> targetsProperty() {
        return targetsProperty;
    }
}
