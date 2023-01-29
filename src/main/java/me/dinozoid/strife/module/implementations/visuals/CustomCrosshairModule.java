package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;

@ModuleInfo(name = "CustomCrosshair", renderName = "CustomCrosshair", category = Category.VISUALS)
public class CustomCrosshairModule extends Module {

    private final EnumProperty<CrosshairMode> crosshairModeProperty = new EnumProperty<>("Mode", CrosshairMode.DEFAULT);
    private final DoubleProperty thicknessProperty = new DoubleProperty("Thickness", 1, 1, 20, 1, Property.Representation.INT, () -> crosshairModeProperty.getValue() != CrosshairMode.SQUARE);
    private final DoubleProperty widthProperty = new DoubleProperty("Width", 4, 1, 50, 1, Property.Representation.INT, () -> crosshairModeProperty.getValue() != CrosshairMode.CIRCLE);
    private final DoubleProperty heightProperty = new DoubleProperty("Height", 4, 1, 50, 1, Property.Representation.INT, () -> crosshairModeProperty.getValue() != CrosshairMode.CIRCLE);
    private final DoubleProperty gapProperty = new DoubleProperty("Gap", 0, 0, 20, 1, Property.Representation.INT, () -> crosshairModeProperty.getValue() == CrosshairMode.DEFAULT);
    private final DoubleProperty radiusProperty = new DoubleProperty("Radius", 1, 1, 20, 1, Property.Representation.INT, () -> crosshairModeProperty.getValue() == CrosshairMode.CIRCLE);
    private final Property<Boolean> smoothProperty = new Property<>("Smooth", true);
    private final Property<Boolean> outlineProperty = new Property<>("Outline", true);

    public enum CrosshairMode {
        DEFAULT, HITMARKER, SQUARE, CIRCLE, POINT, UP_ARROW, DOWN_ARROW
    }

    public EnumProperty<CrosshairMode> crosshairModeProperty() {
        return crosshairModeProperty;
    }

    public DoubleProperty thicknessProperty() {
        return thicknessProperty;
    }
    public DoubleProperty widthProperty() {
        return widthProperty;
    }
    public DoubleProperty heightProperty() {
        return heightProperty;
    }
    public DoubleProperty gapProperty() {
        return gapProperty;
    }
    public DoubleProperty radiusProperty() {
        return radiusProperty;
    }
    public Property<Boolean> smoothProperty() {
        return smoothProperty;
    }
    public Property<Boolean> outlineProperty() {
        return outlineProperty;
    }
}