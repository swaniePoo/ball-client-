package me.dinozoid.strife.event.implementations.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class RenderShaderEvent extends Event {
    private final float partialTicks;
}
