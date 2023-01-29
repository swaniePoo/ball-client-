package me.dinozoid.strife.property.implementations;

import me.dinozoid.strife.property.Property;

import java.awt.*;
import java.util.function.Supplier;

public class ColorProperty extends Property<Color> {

    public ColorProperty(String label, Color value, Supplier<Boolean> dependency) {
        super(label, value, dependency);
    }

    public ColorProperty(String label, Color value) {
        this(label, value, () -> true);
    }

}
