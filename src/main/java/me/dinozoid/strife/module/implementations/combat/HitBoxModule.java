package me.dinozoid.strife.module.implementations.combat;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.implementations.DoubleProperty;

@ModuleInfo(name = "HitBox", renderName = "HitBox", description = "Makes players hitboxes bigger.", category = Category.COMBAT)
public class HitBoxModule extends Module {
    private final DoubleProperty hitboxProperty = new DoubleProperty("HitBox Size", 0.9, 0.1, 1, 0.1);

    public static HitBoxModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(HitBoxModule.class);
    }

    public DoubleProperty hitboxProperty() {
        return hitboxProperty;
    }
}