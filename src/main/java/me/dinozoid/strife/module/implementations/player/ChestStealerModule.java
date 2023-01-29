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
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "ChestStealer", renderName = "ChestStealer", aliases = "Stealer", description = "Automatically take items from chest.", category = Category.PLAYER)
public class ChestStealerModule extends Module {

    private final DoubleProperty clickDelayProperty = new DoubleProperty("Click Delay", 20, 1, 1000, 10, Property.Representation.MILLISECONDS);
    private final DoubleProperty closeDelayProperty = new DoubleProperty("Close Delay", 20, 1, 1000, 10, Property.Representation.MILLISECONDS);
    private final Property<Boolean> rotateCameraProperty = new Property<>("Rotate Camera", false);
    private final Property<Boolean> smartProperty = new Property<>("Smart", true);
    private final Property<Boolean> nameCheckProperty = new Property("Name Check", true);
    private static final Property<Boolean> silentProperty = new Property("Silent", false);

    private final TimerUtil timer = new TimerUtil();


    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        if (event.getState() == EventState.PRE) {
            if (mc.currentScreen instanceof GuiChest) {
                if (rotateCameraProperty.getValue()) {
                    Mouse.setGrabbed(true);
                }
                GuiChest chest = (GuiChest) mc.currentScreen;
                IInventory lowerChestInv = chest.lowerChestInventory;
                if (lowerChestInv.getDisplayName().getUnformattedText().contains("Chest") || lowerChestInv.getDisplayName().getUnformattedText().contains("container.chest") || !nameCheckProperty.getValue()) {
                    if (PlayerUtil.isInventoryFull() || PlayerUtil.isInventoryEmpty(lowerChestInv, true, smartProperty.getValue())) {
                        if (timer.hasElapsed(closeDelayProperty.getValue().longValue()))
                            mc.thePlayer.closeScreen();
                        return;
                    }
                    for (int i = 0; i < lowerChestInv.getSizeInventory(); i++) {
                        if (timer.hasElapsed(clickDelayProperty.getValue().longValue())) {
                            if (PlayerUtil.isValid(lowerChestInv.getStackInSlot(i), true, smartProperty.getValue())) {
                                PlayerUtil.windowClick(chest.inventorySlots.windowId, i, 0, PlayerUtil.ClickType.SHIFT_CLICK);
                                timer.reset();
                                return;
                            }
                        }
                    }
                }
            }
        }
    });

    public static Property<Boolean> silentProperty() {
        return silentProperty;
    }
}
