package me.dinozoid.strife.event.implementations.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.dinozoid.strife.event.Event;

@Getter
@AllArgsConstructor
public class KeyEvent extends Event {
    private final int key;
}
