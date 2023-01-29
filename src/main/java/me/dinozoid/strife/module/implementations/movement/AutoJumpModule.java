package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;

@ModuleInfo(name = "AutoJump", renderName = "AutoJump", category = Category.MOVEMENT)
public class AutoJumpModule extends Module {

    private int ticks;
    private double x, z;

    @EventHandler
    private final Listener<PlayerMotionEvent> motionEventListener = new Listener<>(event -> {
        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedHorizontally && !mc.gameSettings.keyBindJump.isPressed()) {
            mc.gameSettings.keyBindJump.pressed = true;
            ticks = 2;
        }
        if (!mc.thePlayer.capabilities.isFlying && ticks == 0)
            mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isPressed();
        ticks--;
    });

    public void x(double x) {
        this.x = x;
    }
    public void z(double z) {
        this.z = z;
    }
}
