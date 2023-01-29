package me.dinozoid.strife.event.implementations.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.event.Event;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@Getter
@Setter
@AllArgsConstructor
public class DamageBlockEvent extends Event {

    private BlockPos blockPos;
    private EnumFacing facing;
}
