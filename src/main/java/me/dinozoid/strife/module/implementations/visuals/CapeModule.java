package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;

@ModuleInfo(name = "Cape", renderName = "Cape", category = Category.VISUALS)
public class CapeModule extends Module {

    private final Property<Boolean> alphaProperty = new Property<>("Alpha", false);

    public Property<Boolean> alphaProperty() {
        return alphaProperty;
    }
}
