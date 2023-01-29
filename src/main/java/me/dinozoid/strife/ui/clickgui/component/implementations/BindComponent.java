package me.dinozoid.strife.ui.clickgui.component.implementations;

import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.ui.clickgui.component.Component;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;

public class BindComponent extends Component {

    private final Module module;

    public BindComponent(Module module, float x, float y, float width, float height) {
        this(module, x, y, width, height, true);
    }

    public BindComponent(Module module, float x, float y, float width, float height, boolean visible) {
        super(x, y, width, height, visible);
        this.module = module;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        theme.drawBindComponent(module, x, y, width, height, focused);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY) && mouseButton == 0) {
            focused = !focused;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (focused && keyCode == Keyboard.KEY_ESCAPE) {
            module.key(0);
            focused = false;
        }
        if (focused && ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            module.key(keyCode);
            focused = false;
        }
    }
}
