package me.dinozoid.strife.event.implementations.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.event.Event;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

@Getter
@Setter
@AllArgsConstructor
public class BoundingBoxEvent extends Event {
    private BlockPos blockPos;
    private AxisAlignedBB bounds;
}
