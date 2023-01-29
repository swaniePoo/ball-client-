package me.dinozoid.strife.module.implementations.player;

import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(name = "Timer", renderName = "Timer", description = "Set the speed of the game.", category = Category.PLAYER)
public class TimerModule extends Module {

    private final DoubleProperty timerSpeed = new DoubleProperty("Timer Speed", 1.6, 0.1, 10, 0.25, Property.Representation.DOUBLE);
    private final TimerUtil timer = new TimerUtil();

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        if (event.isPost()) {
//           double x = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
//           double z = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
//           mc.timer.timerSpeed = 0.75F;
//           if(timer.hasElapsed(25)){
//               PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z, true));
//               mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
//               timer.reset();
//           }
            mc.timer.timerSpeed = timerSpeed.getValue().floatValue() + MovementUtil.getRandomHypixelValuesFloat();
        }
    });

    @Override
    public void init() {
        super.init();
        addValueChangeListener(timerSpeed);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer.reset();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
        super.onDisable();
    }

}
