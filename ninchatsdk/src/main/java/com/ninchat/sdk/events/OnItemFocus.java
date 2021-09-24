package com.ninchat.sdk.events;

public class OnItemFocus {
    public int position;
    public boolean actionDone;
    public OnItemFocus(final int position, final boolean actionDone) {
        this.position = position;
        this.actionDone = actionDone;
    }
}
