package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.MovePlayerEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.util.player.MovementUtil;

@ModuleInfo(name = "Sprint", renderName = "Sprint", category = Category.MOVEMENT)
public class SprintModule extends Module {

    private final Property<Boolean> omniProperty = new Property<>("Omni", false);


    @EventHandler
    private final Listener<MovePlayerEvent> movePlayerEventListener = new Listener<>(event -> {
        if(mc.thePlayer == null || mc.thePlayer.ticksExisted < 5) return;
        mc.thePlayer.setSprinting(MovementUtil.canSprint());
    });

    @Override
    public void onDisable() {
        if(mc.thePlayer != null) mc.thePlayer.setSprinting(mc.gameSettings.keyBindSprint.isPressed());
        super.onDisable();
    }

    @Override
    public void init() {
        super.init();
    }

}
