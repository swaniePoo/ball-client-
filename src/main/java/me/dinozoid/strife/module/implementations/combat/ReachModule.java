package me.dinozoid.strife.module.implementations.combat;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;

@ModuleInfo(name = "Reach", renderName = "Reach", description = "Allows you to attack people from farther away.", category = Category.COMBAT)
public class ReachModule extends Module {

    private final DoubleProperty reachProperty = new DoubleProperty("Reach", 3.3, 1, 6, 0.1, Property.Representation.DOUBLE);

    public static ReachModule getInstance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(ReachModule.class);
    }

    @Override
    public void init() {
        super.init();
        addValueChangeListener(reachProperty);
    }

    public DoubleProperty reachProperty() {
        return reachProperty;
    }
}
