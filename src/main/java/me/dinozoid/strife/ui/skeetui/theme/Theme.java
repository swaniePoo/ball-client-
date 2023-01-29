package me.dinozoid.strife.ui.skeetui.theme;

import me.dinozoid.strife.ui.skeetui.component.implementations.SidebarComponent;

public interface Theme {

    /* SkeetUI Ideas

        We need panels, non abstract class
        each panel contains a list of components,


        We need a sidebar component which contains the categories

     */

    void drawSidebarComponent(int mouseX, int mouseY, SidebarComponent component);
    void drawSidebarButtonComponent(int mouseX, int mouseY, SidebarComponent.SidebarButtonComponent component);

}
