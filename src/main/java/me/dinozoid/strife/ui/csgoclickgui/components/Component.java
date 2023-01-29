package me.dinozoid.strife.ui.csgoclickgui.components;

import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.ui.csgoclickgui.IDrawableComponent;

public abstract class Component<T extends Property<?>> implements IDrawableComponent {
    public T property;
    public float x, y, width, height;

    public Component(T property, float x, float y, float width, float height) {
        this.property = property;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
