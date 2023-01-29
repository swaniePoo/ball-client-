package me.dinozoid.strife.event.implementations.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.dinozoid.strife.event.Event;
import net.minecraft.entity.EntityLivingBase;

@Getter
@AllArgsConstructor
public class PlayerJumpEvent extends Event {

    private final EntityLivingBase entity;
}
