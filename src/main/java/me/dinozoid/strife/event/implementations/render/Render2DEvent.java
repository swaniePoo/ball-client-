package me.dinozoid.strife.event.implementations.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.dinozoid.strife.event.Event;
import net.minecraft.client.gui.ScaledResolution;

@Getter
@AllArgsConstructor
public class Render2DEvent extends Event {

    private final float partialTicks;
    private final ScaledResolution scaledResolution;
}
