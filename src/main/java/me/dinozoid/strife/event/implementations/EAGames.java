package me.dinozoid.strife.event.implementations;

import me.dinozoid.strife.event.Event;

public class EAGames extends Event {
    private int currentItem;

    public EAGames(int currentItem) {
        this.currentItem = currentItem;
    }

    public int getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }
}
