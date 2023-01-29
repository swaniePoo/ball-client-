package me.dinozoid.strife.ui.skeetui.component.implementations;

import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.ui.skeetui.component.Component;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

public class SidebarComponent extends Component {

    protected final List<Component> components = new ArrayList<>();

    public SidebarComponent(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    @Override
    public void init() {
        // first creation

    }

    @Override
    public void reset() {
        // we call this method on reopen of gui
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        // draw
        // theme.drawSidebarComponent(this);
        theme.drawSidebarComponent(mouseX, mouseY, this);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // mouse click (self explained)
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        // mouse release (self explained)
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // key typed (self explained)
    }


    public static class SidebarButtonComponent extends Component {

        private Category category;
        private boolean selected;

        public SidebarButtonComponent(Category category, float x, float y, float width, float height) {
            super(x, y, width, height);
            this.category = category;
        }

        @Override
        public void reset() {

        }

        @Override
        public void drawScreen(int mouseX, int mouseY) {
            theme.drawSidebarButtonComponent(mouseX, mouseY, this);
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        }

        @Override
        public void mouseReleased(int mouseX, int mouseY, int state) {

        }

        @Override
        public void keyTyped(char typedChar, int keyCode) {

        }

        public Category getCategory() {
            return category;
        }
    }

}
