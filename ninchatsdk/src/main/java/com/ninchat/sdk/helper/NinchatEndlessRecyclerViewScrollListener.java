package com.ninchat.sdk.helper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class NinchatEndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    public NinchatEndlessRecyclerViewScrollListener() { }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            onUpdateView(recyclerView, newState);
        }
        super.onScrollStateChanged(recyclerView, newState);
    }

    public abstract void onUpdateView(RecyclerView recyclerView, int newState);


}
