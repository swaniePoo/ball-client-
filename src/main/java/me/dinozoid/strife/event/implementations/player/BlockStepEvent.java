package me.dinozoid.strife.event.implementations.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.event.Event;

@Setter
@Getter
@AllArgsConstructor
public class BlockStepEvent extends Event {

    private EventState state;
    private float height;
}