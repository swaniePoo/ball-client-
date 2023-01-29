package me.dinozoid.strife.module.implementations.misc;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.util.player.PlayerUtil;

@ModuleInfo(name = "ResetVL", renderName = "Reset VL", category = Category.MISC)
public class ResetVLModule extends Module {
    private int jumped;
    private double y;

    @EventHandler
    private final Listener<PlayerMotionEvent> playerMotionEventListener = new Listener<>(event ->{
        if(mc.thePlayer.onGround) {
            if(jumped <= 25) {
                mc.thePlayer.motionY = 0.11;
                jumped++;
            }
        }
        if(jumped <= 25) {
            mc.thePlayer.posY = y;
            mc.timer.timerSpeed = 2.25f;
        }else{
            mc.timer.timerSpeed = 1;
            PlayerUtil.sendMessageWithPrefix("Player VL Should Have Been Reset!");
            toggle();
        }
    });


    @Override
    public void onEnable() {
        jumped = 0;
        y = mc.thePlayer.posY;
        PlayerUtil.sendMessageWithPrefix("You can either move or stand still");
        super.onEnable();
    }
}
