package me.dinozoid.strife.module.implementations.combat;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoClicker", renderName = "AutoClicker", description = "Clicks for you.", category = Category.COMBAT)
public class AutoClickerModule extends Module {

    private final DoubleProperty minimumCPS = new DoubleProperty("Minimum CPS", 12, 1, 20, 1, Property.Representation.INT);
    private final DoubleProperty maximumCPS = new DoubleProperty("Maximum CPS", 14, 1, 20, 1, Property.Representation.INT);

    private final Property<Boolean> rightMouse = new Property("Right Mouse", false);
    private final Property<Boolean> onlySword = new Property("Only Sword", false);

    private final TimerUtil timer = new TimerUtil();
    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        if (mc.currentScreen != null || (!PlayerUtil.isHoldingSword() && onlySword.getValue())) return;
        if (Mouse.isButtonDown(0)) {
            if (timer.hasElapsed(RandomUtils.nextInt(minimumCPS.getValue().intValue(), maximumCPS.getValue().intValue()) / 1000)) {
                mc.leftClickCounter = 0;
                mc.clickMouse();
                timer.reset();
            }
        }
        if (Mouse.isButtonDown(2) && rightMouse.getValue()) {
            if (timer.hasElapsed(RandomUtils.nextInt(minimumCPS.getValue().intValue(), maximumCPS.getValue().intValue()) / 1000)) {
                mc.rightClickMouse();
                timer.reset();
            }
        }
    });

    @Override
    public void init() {
        super.init();
        setSuffix(minimumCPS.getValue().intValue() + "-" + maximumCPS.getValue().intValue());
        minimumCPS.addValueChange((oldValue, value) -> setSuffix(value.intValue() + "-" + maximumCPS.getValue().intValue()));
        maximumCPS.addValueChange((oldValue, value) -> setSuffix(value.intValue() + "-" + minimumCPS.getValue().intValue()));
    }

}
