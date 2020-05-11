package com.ninchat.sdk.helper;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class NinchatEndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    private boolean updateData = false;
    private int index;
    private boolean updated;
    private boolean removed;

    @NonNull
    private final Callback callback;

    public NinchatEndlessRecyclerViewScrollListener(@NotNull Callback callback) {
        this.callback = callback;
    }

    public void setData(final int index, final boolean updated, final boolean removed) {
        updateData = true;
        this.index = index;
        this.updated = updated;
        this.removed = removed;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && updateData) {
            this.callback.requiredMessageUpdate(index, updated, removed);
            updateData = false;
        }
        super.onScrollStateChanged(recyclerView, newState);
    }

    public interface Callback {
        void requiredMessageUpdate(int index, boolean updated, boolean removed);
    }

}
