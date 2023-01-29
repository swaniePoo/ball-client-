package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.implementations.EnumProperty;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

@ModuleInfo(name = "Fullbright", renderName = "Fullbright", description = "Self explanatory.", category = Category.VISUALS)
public class FullbrightModule extends Module {

    private final EnumProperty<FullbrightMode> mode = new EnumProperty<>("Mode", FullbrightMode.POTION);
    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        switch (mode.getValue()) {
            case GAMMA:
                mc.gameSettings.gammaSetting = 10000f;
                break;
            case POTION:
                mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 5201, 9));
                break;
        }
    });
    private float oldGamma;

    @Override
    public void init() {
        super.init();
        addValueChangeListener(mode);
    }

    @Override
    public void onEnable() {
        if (mode.getValue() == FullbrightMode.GAMMA)
            oldGamma = mc.gameSettings.gammaSetting;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        switch (mode.getValue()) {
            case GAMMA:
                mc.gameSettings.gammaSetting = oldGamma;
                break;
            case POTION:
                mc.thePlayer.removePotionEffect(Potion.nightVision.id);
                break;
        }
        super.onDisable();
    }

    private enum FullbrightMode {
        GAMMA, POTION
    }

}
