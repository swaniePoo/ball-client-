package me.dinozoid.strife.event.implementations.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.dinozoid.strife.event.Event;

@Getter
@AllArgsConstructor
public class RenderGUIEvent extends Event {

    private final int mouseX;
    private final int mouseY;
    private final float partialTicks;
}
