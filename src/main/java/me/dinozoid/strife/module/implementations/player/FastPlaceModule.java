package me.dinozoid.strife.module.implementations.player;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;

@ModuleInfo(name = "FastPlace", renderName = "FastPlace", description = "Place blocks faster.", category = Category.PLAYER)
public class FastPlaceModule extends Module {

    private final DoubleProperty delayProperty = new DoubleProperty("Delay", 3, 1, 4, 1, Property.Representation.INT);

    @Override
    public void init() {
        super.init();
        addValueChangeListener(delayProperty);
    }

    public static FastPlaceModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(FastPlaceModule.class);
    }

    public DoubleProperty delayProperty() {
        return delayProperty;
    }
}
