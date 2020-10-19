package com.ninchat.sdk.helper

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class NinchatQuestionnaireItemDecoration(private val spaceTop: Int, private val spaceLeft: Int, private val spaceRight: Int) : ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.top = spaceTop
        outRect.left = spaceLeft
        outRect.right = spaceRight
    }
}