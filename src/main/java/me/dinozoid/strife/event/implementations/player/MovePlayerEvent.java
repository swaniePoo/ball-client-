package me.dinozoid.strife.event.implementations.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.event.Event;

@AllArgsConstructor
@Getter
@Setter
public class MovePlayerEvent extends Event {
    private double x, y, z;
}
