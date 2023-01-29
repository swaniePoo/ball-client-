package me.dinozoid.strife.event.implementations.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.dinozoid.strife.event.Event;

@Getter
@AllArgsConstructor
public class WindowClickEvent extends Event {

    private final int windowId, slotId, mouseButton, mode;

}
