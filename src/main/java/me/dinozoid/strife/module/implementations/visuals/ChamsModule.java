package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.ColorProperty;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.render.RenderUtil;

import java.awt.*;

@ModuleInfo(name = "Chams", renderName = "Chams", category = Category.VISUALS)
public class ChamsModule extends Module {

    private final EnumProperty<RenderUtil.ColorMode> colorModeProperty = new EnumProperty<>("Color", RenderUtil.ColorMode.ASTOLFO);
    private final MultiSelectEnumProperty<PlayerUtil.Target> targetsProperty = new MultiSelectEnumProperty<>("Targets", PlayerUtil.Target.PLAYERS);

    private final Property<Boolean> materialProperty = new Property<>("Material", false);
    private final Property<Boolean> coloredProperty = new Property<>("Colored", true);
    private final Property<Boolean> handProperty = new Property<>("Hand", false);
    private final EnumProperty<RenderUtil.ColorMode> handColorModeProperty = new EnumProperty<>("Hand Color", RenderUtil.ColorMode.ASTOLFO, () -> handProperty.getValue());
    private final ColorProperty handColorProperty = new ColorProperty("Color", new Color(209, 50, 50), () -> handColorModeProperty.getValue() == RenderUtil.ColorMode.STATIC || handColorModeProperty.getValue() == RenderUtil.ColorMode.PULSE || handColorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);
    private final ColorProperty secondHandColorProperty = new ColorProperty("Color", new Color(209, 50, 50), () -> colorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);

    private final ColorProperty colorProperty = new ColorProperty("Color", new Color(209, 50, 50), () -> colorModeProperty.getValue() == RenderUtil.ColorMode.STATIC || colorModeProperty.getValue() == RenderUtil.ColorMode.PULSE || colorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);
    private final ColorProperty secondColorProperty = new ColorProperty("Color", new Color(29, 205, 200), () -> colorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);

    private final ColorProperty hiddenColorProperty = new ColorProperty("Hidden Color", new Color(209, 50, 50), () -> colorModeProperty.getValue() == RenderUtil.ColorMode.STATIC || colorModeProperty.getValue() == RenderUtil.ColorMode.PULSE || colorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);
    private final ColorProperty hiddenSecondColorProperty = new ColorProperty("Hidden Color", new Color(29, 205, 200), () -> colorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);
    private final DoubleProperty alphaProperty = new DoubleProperty("Alpha", 255, 0, 255, 1, Property.Representation.INT);

    private final DoubleProperty handAlphaProperty = new DoubleProperty("Alpha", 255, 0, 255, 1, Property.Representation.INT);

    @Override
    public void init() {
        super.init();
    }

    public DoubleProperty alphaProperty() {
        return alphaProperty;
    }

    public DoubleProperty handAlphaProperty() {
        return handAlphaProperty;
    }

    public Property<Boolean> handProperty() {
        return handProperty;
    }

    public Property<Boolean> materialProperty() {
        return materialProperty;
    }

    public Property<Boolean> coloredProperty() {
        return coloredProperty;
    }

    public MultiSelectEnumProperty<PlayerUtil.Target> targetsProperty() {
        return targetsProperty;
    }

    public int getHiddenColor(int index) {
        return RenderUtil.getColor(colorModeProperty, index, hiddenColorProperty.getValue(), hiddenSecondColorProperty.getValue());
    }

    public int getColor(int index) {
        return RenderUtil.getColor(colorModeProperty, index, colorProperty.getValue(), secondColorProperty.getValue());
    }

    public int getHandColor(int index) {
        return RenderUtil.getColor(handColorModeProperty, index, handColorProperty.getValue(), secondHandColorProperty.getValue());
    }

}
