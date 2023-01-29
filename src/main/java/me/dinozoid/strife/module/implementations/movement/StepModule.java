package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.BlockStepEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.MovementUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

import java.util.concurrent.TimeUnit;

@ModuleInfo(name = "Step", renderName = "Step", description = "Makes you step up blocks.", category = Category.MOVEMENT)
public class StepModule extends Module {

    private final EnumProperty<StepMode> stepModeProperty = new EnumProperty<>("Mode", StepMode.NCP);
    private final float[] values = new float[] { 0.42f, 0.753f };
    private boolean stepping;

    @EventHandler
    private final Listener<BlockStepEvent> stepEventListener = new Listener<>(event -> {
        switch (stepModeProperty.getValue()) {
            case NCP: {
                if (event.getState() == EventState.PRE) {
                    if (mc.thePlayer.movementInput.jump || !mc.thePlayer.isCollidedVertically) return;
                    event.setHeight(1);
                    stepping = true;
                } else if (event.getState() == EventState.POST && stepping && event.getHeight() > 0.5f) {
                    if (mc.thePlayer.movementInput.jump || !mc.thePlayer.isCollidedVertically) return;
                    float height = event.getHeight();
                    double x = mc.thePlayer.posX;
                    double y = mc.thePlayer.posY;
                    double z = mc.thePlayer.posZ;
                    for (float value : values) {
                        PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x, y + value * height, z, mc.thePlayer.onGround));
                    }
                    mc.timer.timerSpeed = 1f / values.length + (float) MovementUtil.getRandomHypixelValues();
                    Client.INSTANCE.getScheduledExecutorService().schedule(() -> {
                        mc.timer.timerSpeed = 1f;
                        stepping = false;
                    }, values.length * 50, TimeUnit.MILLISECONDS);
                }
                break;
            }
        }
    });

    public enum StepMode {
        NCP
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.thePlayer.stepHeight = 0.625f;
    }
}