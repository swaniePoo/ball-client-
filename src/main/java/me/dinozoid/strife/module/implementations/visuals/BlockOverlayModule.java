package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;

@ModuleInfo(name = "BlockOverlay", renderName = "BlockOverlay", category = Category.VISUALS)
public class BlockOverlayModule extends Module {
    private final Property<Boolean> outlineProperty = new Property<>("Outline", false);

    public Property<Boolean> getOutlineProperty() {
        return outlineProperty;
    }
}
