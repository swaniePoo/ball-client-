package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.util.render.RenderUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.Display;

@ModuleInfo(name = "Scoreboard", renderName = "Scoreboard", category = Category.VISUALS)
public class ScoreboardModule extends Module {

    private float dragX, dragY, origX, origY, width, height;
    private boolean dragging, hovered;

    private final Property<Boolean> visibleProperty = new Property<>("Visible", true);
    private final Property<Boolean> dynamicProperty = new Property<>("Dynamic", true);
    private final DoubleProperty xPositionProperty = new DoubleProperty("X", 0, 0, Display.getWidth() + 10, 1, Property.Representation.INT);
    private final DoubleProperty yPositionProperty = new DoubleProperty("Y", 0, 0, Display.getHeight() - 52, 1, Property.Representation.INT);

    public static ScoreboardModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(ScoreboardModule.class);
    }

    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!toggled()) return;
        dynamicProperty.setValue(false);
        if (RenderUtil.inBounds(xPositionProperty.getValue().floatValue(), yPositionProperty.getValue().floatValue(), width + xPositionProperty.getValue().floatValue(), yPositionProperty.getValue().floatValue() + height, mouseX, mouseY)) {
            if (mouseButton == 0) {
                dragging = true;
                dragX = (xPositionProperty.getValue().floatValue() - mouseX);
                dragY = (yPositionProperty.getValue().floatValue() - mouseY);
            }
        }
    }

    public void onMouseRelease(int mouseX, int mouseY, int state) {
        if (!toggled()) return;
        dragging = false;
        hovered = false;
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        if (xPositionProperty.max() != scaledResolution.getScaledWidth())
            xPositionProperty.max(scaledResolution.getScaledWidth());
        if (yPositionProperty.max() != scaledResolution.getScaledHeight())
            yPositionProperty.max(scaledResolution.getScaledHeight());
        if (!toggled()) return;
        if (dragging) {
            xPositionProperty.setValue((double) (mouseX + dragX));
            yPositionProperty.setValue((double) (mouseY + dragY));
            origX = xPositionProperty.getValue().floatValue();
            origY = yPositionProperty.getValue().floatValue();
        }
        if(mc.currentScreen instanceof GuiChat) {
            float x = xPositionProperty.getValue().floatValue();
            float y = yPositionProperty.getValue().floatValue();
            hovered = RenderUtil.inBounds(x, y, x + width, y + height, mouseX, mouseY);
        }
    }

    public boolean hovered() {
        return hovered;
    }
    public void width(float width) {
        this.width = width;
    }
    public void height(float height) {
        this.height = height;
    }
    public float width() {
        return width;
    }
    public float height() {
        return height;
    }
    public Property<Boolean> dynamicProperty() {
        return dynamicProperty;
    }
    public Property<Boolean> visibleProperty() {
        return visibleProperty;
    }
    public DoubleProperty xPositionProperty() {
        return xPositionProperty;
    }
    public DoubleProperty yPositionProperty() {
        return yPositionProperty;
    }
}
