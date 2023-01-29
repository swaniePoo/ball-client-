package me.dinozoid.strife.module.implementations.player;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.module.implementations.player.hackerdetector.Check;
import me.dinozoid.strife.module.implementations.player.hackerdetector.impl.KeepSprintA;
import me.dinozoid.strife.module.implementations.player.hackerdetector.impl.MotionA;
import me.dinozoid.strife.module.implementations.player.hackerdetector.impl.SpeedA;
import me.dinozoid.strife.module.implementations.player.hackerdetector.impl.SpeedB;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "HackerDetector", renderName = "HackerDetector", description = "the name says it.", category = Category.PLAYER)
public class HackerDetectorModule extends Module {

    private final List<Check> checks = new ArrayList<>();
    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerEventListener = new Listener<>(event -> {
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (e instanceof EntityLivingBase) {
                EntityLivingBase base = (EntityLivingBase) e;
                if (base instanceof EntityPlayer) {
                    this.checks.forEach(c -> c.handleCheck(base));
                }
            }
        }
    });

    @Override
    public void init() {
        super.init();
        checks.add(new SpeedA());
        checks.add(new SpeedB());
        checks.add(new KeepSprintA());
        checks.add(new MotionA());
    }
}
