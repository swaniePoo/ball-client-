package me.dinozoid.strife.ui.clickgui;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.implementations.visuals.ClickGUIModule;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.ColorProperty;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.shader.implementations.BlurShader;
import me.dinozoid.strife.ui.clickgui.component.Component;
import me.dinozoid.strife.ui.clickgui.component.implementations.*;
import me.dinozoid.strife.ui.clickgui.panel.implementations.CategoryPanel;
import me.dinozoid.strife.ui.clickgui.panel.implementations.ModulePanel;
import me.dinozoid.strife.ui.clickgui.panel.implementations.MultiSelectPanel;
import me.dinozoid.strife.ui.clickgui.theme.Theme;
import me.dinozoid.strife.ui.clickgui.theme.implementations.RetardTheme;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StrifeClickGUI extends GuiScreen {

    private final Theme currentTheme;

    private final List<Component> objects = new ArrayList<>();

    private final float componentWidth = 110;
    private final float componentHeight = 15;

    private final BlurShader blurShader;
    private final ClickGUIModule clickGUIModule;

    public StrifeClickGUI() {
        clickGUIModule = Client.INSTANCE.getModuleRepository().moduleBy(ClickGUIModule.class);
        currentTheme = new RetardTheme();
        blurShader = new BlurShader(clickGUIModule.blurIntensityProperty().getValue().intValue());
        float posX = 6;
        float posY = 4;
        for (Category category : Category.values()) {
            objects.add(new CategoryPanel(category, posX, posY, componentWidth, componentHeight) {
                @Override
                public void init() {
                    for (Module module : Client.INSTANCE.getModuleRepository().modules()) {
                        if (module.category() == category) {
                            components().add(new ModulePanel(module, x, y, componentWidth, componentHeight) {
                                @Override
                                public void init() {
                                    components.add(new BindComponent(module, x, y, componentWidth, componentHeight));
                                    for (Property property : Module.propertyRepository().propertiesBy(module.getClass())) {
                                        if (property.getValue() instanceof Boolean)
                                            components.add(new BooleanComponent(property, x, y, componentWidth, componentHeight, property.available()));
                                        if (property instanceof EnumProperty)
                                            components.add(new EnumComponent((EnumProperty) property, x, y, componentWidth, componentHeight, property.available()));
                                        if (property instanceof DoubleProperty)
                                            components.add(new SliderComponent((DoubleProperty) property, x, y, componentWidth, componentHeight, property.available()));
                                        if (property instanceof MultiSelectEnumProperty)
                                            components.add(new MultiSelectPanel((MultiSelectEnumProperty) property, x, y, componentWidth, componentHeight));
                                        if (property instanceof ColorProperty)
                                            components.add(new ColorPickerComponent((ColorProperty) property, x, y, componentWidth, componentHeight * 5));
                                        property.addValueChange((oldValue, value) -> updateComponents());
                                    }
                                    updateComponents();
                                }
                            });
                        }
                    }
                }
            });
            posX += componentWidth + 3;
        }
    }

    @Override
    public void initGui() {
        objects.forEach(Component::reset);
    }

    @Override
    public void onGuiClosed() {
        objects.forEach(panel -> {
            if (panel.visible()) panel.mouseReleased(0, 0, 0);
        });
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (clickGUIModule.blurProperty().getValue()) {
            blurShader.setRadius(clickGUIModule.blurIntensityProperty().getValue().intValue());
            blurShader.blur();
        }
        objects.forEach(panel -> {
            if (panel.visible()) panel.drawScreen(mouseX, mouseY);
        });
//        mc.fontRendererObj.drawString("FPS: " + Minecraft.getDebugFPS(), 0, 0, -1);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        objects.forEach(panel -> {
            if (panel.visible()) panel.mouseClicked(mouseX, mouseY, mouseButton);
        });
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        objects.forEach(panel -> {
            if (panel.visible()) panel.mouseReleased(mouseX, mouseY, state);
        });
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean focused = false;
        for (Component panel : objects)
            if (panel.visible() && panel.focused())
                focused = true;
        if (!focused) {
            super.keyTyped(typedChar, keyCode);
        }
        objects.forEach(panel -> {
            if (panel.visible()) panel.keyTyped(typedChar, keyCode);
        });
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
