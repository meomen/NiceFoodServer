package com.vuducminh.nicefoodserver.eventbus;

public class AddonSizeEditEvent {
    private boolean isAddon;
    private int position;

    public AddonSizeEditEvent(boolean isAddon, int position) {
        this.isAddon = isAddon;
        this.position = position;
    }

    public boolean isAddon() {
        return isAddon;
    }

    public void setAddon(boolean addon) {
        isAddon = addon;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
