package me.dinozoid.strife.module.implementations.player;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.module.implementations.combat.KillAuraModule;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "NoSlowdown", renderName = "NoSlowdown", category = Category.PLAYER, aliases = "NoSlow")
public class NoSlowdownModule extends Module {

    private final EnumProperty<NoSlowdownMode> modeProperty = new EnumProperty<>("Mode", NoSlowdownMode.NCP);

    private final TimerUtil timer = new TimerUtil();
    private boolean blocking;

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @EventHandler
    private final Listener<PacketOutboundEvent> packetOutboundListener = new Listener<>(event -> {
        if (event.getPacket() instanceof C07PacketPlayerDigging)
            blocking = false;
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement)
            blocking = true;
    });

    @EventHandler
    private final Listener<PacketInboundEvent> packetInboundListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case HYPIXEL: {
                if (mc.theWorld == null || mc.thePlayer == null) return;
                if (event.getPacket() instanceof S30PacketWindowItems) {
                    if (mc.thePlayer.isUsingItem()) {
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                        event.cancel();
                    } else {
                        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(-1, -1, -1), EnumFacing.DOWN));
                    }
                }
                break;
            }
        }
    });

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerEvent = new Listener<>(event -> {
        if (KillAuraModule.instance().target() == null && MovementUtil.isMoving() && PlayerUtil.isHoldingSword() && mc.thePlayer.isBlocking()) {
            if (event.getState() == EventState.PRE) {
                switch (modeProperty.getValue()) {
                    case HYPIXEL_DELAY: {
                        if (timer.hasElapsed(200) && blocking) {
                            double value = Math.random() * ThreadLocalRandom.current().nextDouble(-Double.MIN_VALUE, Double.MAX_VALUE);
                            PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(value, value, value), EnumFacing.DOWN));
                            timer.reset();
                        }
                        break;
                    }
                    case NCP:
                        PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        break;
                }
            } else {
                switch (modeProperty.getValue()) {
                    case NCP: {
                        PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        break;
                    }
                    case HYPIXEL_DELAY: {
                        if (timer.hasElapsed(200) && !blocking) {
                            PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                            timer.reset();
                        }
                        break;
                    }
                    case HYPIXEL: {
                        if (event.getState() == EventState.POST) {
                            if (mc.thePlayer.isUsingItem()) {
                                PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem < 8 ? mc.thePlayer.inventory.currentItem + 1 : mc.thePlayer.inventory.currentItem - 1));
                                PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                            }
                        }
                        break;
                    }
                }
            }
        }
    });

    public static NoSlowdownModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(NoSlowdownModule.class);
    }

    @Override
    public void init() {
        super.init();
        addValueChangeListener(modeProperty, () -> modeProperty.getValue() != NoSlowdownMode.NCP);
    }

    public enum NoSlowdownMode {
        VANILLA, NCP, HYPIXEL, HYPIXEL_DELAY
    }

}