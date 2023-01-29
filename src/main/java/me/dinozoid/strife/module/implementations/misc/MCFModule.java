package me.dinozoid.strife.module.implementations.misc;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.system.MouseEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.target.implementations.Friend;
import me.dinozoid.strife.util.player.PlayerUtil;
import net.minecraft.entity.Entity;

@ModuleInfo(name = "MCF", renderName = "MCF", aliases = "MiddleClickFriend", description = "Middle click to friend.", category = Category.MISC)
public class MCFModule extends Module {

    @EventHandler
    private final Listener<MouseEvent> mouseListener = new Listener<>(event -> {
        if (event.getMouseButton() == 1) {
            if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null) {
                Entity entity = mc.objectMouseOver.entityHit;
                if (Client.INSTANCE.getTargetRepository().targetBy(entity.getName()) == null) {
                    Client.INSTANCE.getTargetRepository().add(new Friend(entity.getName()));
                    PlayerUtil.sendMessage("&c[MCF]&f Added friend.");
                } else {
                    Client.INSTANCE.getTargetRepository().remove(Client.INSTANCE.getTargetRepository().targetBy(entity.getName()));
                    PlayerUtil.sendMessage("&c[MCF]&f Removed friend.");
                }
            }
        }
    });

}
