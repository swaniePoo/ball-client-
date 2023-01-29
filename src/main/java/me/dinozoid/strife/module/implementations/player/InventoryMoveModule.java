package me.dinozoid.strife.module.implementations.player;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;

@ModuleInfo(name = "InventoryMove", renderName = "InventoryMove", description = "Move with your inventory open.", aliases = {"InvMove", "GuiMove"}, category = Category.PLAYER)
public class InventoryMoveModule extends Module {
    public static InventoryMoveModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(InventoryMoveModule.class);
    }
}
