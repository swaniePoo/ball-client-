package me.dinozoid.strife.module.implementations.combat;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.module.implementations.movement.SpeedModule;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.entity.Entity;

@ModuleInfo(name = "Criticals", renderName = "Criticals", description = "Always do a critical hit.", category = Category.COMBAT)
public class CriticalsModule extends Module {

    private final EnumProperty<CriticalsMode> modeProperty = new EnumProperty<>("Mode", CriticalsMode.HYPIXEL);

    private final TimerUtil timer = new TimerUtil();

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case HYPIXEL: {
                if (shouldCrit() && mc.thePlayer.onGround && getTarget().hurtResistantTime != 20 && !SpeedModule.instance().toggled()) {
                    if (event.isPre()) {
                        event.setPosY(event.getPosY() + 0.003 + MovementUtil.getRandomHypixelValues());
                        if (timer.hasElapsed(500)) {
                            PlayerUtil.sendMessageWithPrefix("Crit");
                            event.setPosY(event.getPosY() + 0.001 + MovementUtil.getRandomHypixelValues());
                            timer.reset();
                        }
                        event.setGround(false);
                    }
                }
                break;
            }
        }
    });

    @EventHandler
    private final Listener<PacketOutboundEvent> packetListener = new Listener<>(event -> {

    });

    @Override
    public void init() {
        super.init();
        addValueChangeListener(modeProperty);
    }

    private Entity getTarget() {
        return KillAuraModule.instance().target();
    }

    private boolean shouldCrit() {
        return getTarget() != null && !PlayerUtil.isInLiquid() && !MovementUtil.isInsideBlock();
    }

    public enum CriticalsMode {
        HYPIXEL
    }

}
