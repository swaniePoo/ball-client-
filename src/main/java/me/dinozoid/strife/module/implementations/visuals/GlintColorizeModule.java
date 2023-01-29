package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;

@ModuleInfo(name = "GlintColorize", renderName = "GlintColorize", category = Category.VISUALS)
public class GlintColorizeModule extends Module {

    public static GlintColorizeModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(GlintColorizeModule.class);
    }

}
