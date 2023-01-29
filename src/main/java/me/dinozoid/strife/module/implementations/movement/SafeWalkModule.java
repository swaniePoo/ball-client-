package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;

@ModuleInfo(name = "SafeWalk", renderName = "SafeWalk", description = "Don't fall off.", category = Category.MOVEMENT)
public class SafeWalkModule extends Module {

    public static SafeWalkModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(SafeWalkModule.class);
    }
}
