package com.ninchat.sdk.helper;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class NinchatQuestionnaireItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public NinchatQuestionnaireItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = space;
    }
}
