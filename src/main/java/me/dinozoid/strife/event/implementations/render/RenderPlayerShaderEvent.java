package me.dinozoid.strife.event.implementations.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.dinozoid.strife.event.Event;

@Getter
@AllArgsConstructor
public final class RenderPlayerShaderEvent extends Event {
    private final float partialTicks;
}