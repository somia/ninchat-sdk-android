package com.ninchat.sdk.helper;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class NinchatQuestionnaireItemDecoration extends RecyclerView.ItemDecoration {
    private int spaceTop;
    private int spaceLeft;
    private int spaceRight;

    public NinchatQuestionnaireItemDecoration(int spaceTop, int spaceLeft, int spaceRight) {
        this.spaceTop = spaceTop;
        this.spaceLeft = spaceLeft;
        this.spaceRight = spaceRight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = spaceTop;
        outRect.left = spaceLeft;
        outRect.right = spaceRight;
    }
}
