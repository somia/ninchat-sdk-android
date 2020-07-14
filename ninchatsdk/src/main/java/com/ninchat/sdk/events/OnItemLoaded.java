package com.ninchat.sdk.events;

import android.support.v7.widget.RecyclerView;

public class OnItemLoaded {
    public int position;
    public OnItemLoaded(final int position) {
        this.position = position;
    }
}
