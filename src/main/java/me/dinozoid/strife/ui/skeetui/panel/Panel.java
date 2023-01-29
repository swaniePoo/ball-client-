package me.dinozoid.strife.ui.skeetui.panel;

import me.dinozoid.strife.ui.skeetui.component.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class Panel extends Component {

    protected final List<Component> components = new ArrayList<>();

    public Panel(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public Panel(float x, float y, float width, float height, boolean visible) {
        super(x, y, width, height, visible);
    }

    @Override
    public void init() {
        components.forEach(Component::init);
    }

    @Override
    public void reset() {
        components.forEach(Component::reset);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        components.forEach(component -> {
            if(component.isVisible())
                component.drawScreen(mouseX, mouseY);
        });
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        components.forEach(component -> {
            if(component.isVisible() && component.isHovered(mouseX, mouseY))
                component.mouseClicked(mouseX, mouseY, mouseButton);
        });
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        components.forEach(component -> {
            if(component.isVisible())
                component.mouseReleased(mouseX, mouseY, state);
        });
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        components.forEach(component -> {
            if(component.isVisible())
                component.keyTyped(typedChar, keyCode);
        });
    }

}
