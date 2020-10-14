package com.ninchat.sdk.ninchatquestionnaire.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.ninchatquestionnaire.model.NinchatButtonViewModel
import com.ninchat.sdk.ninchatquestionnaire.presenter.NinchatButtonViewPresenter

class NinchatButtonViewHolder(
        itemView: View,
        ninchatButtonViewModel: NinchatButtonViewModel,
) : RecyclerView.ViewHolder(itemView) {
    val ninchatButtonViewPresenter = NinchatButtonViewPresenter(ninchatButtonViewModel)

    fun initialize() {
        ninchatButtonViewPresenter.initiateView(itemView = itemView)
        ninchatButtonViewPresenter.renderCurrentView(itemView = itemView)
    }

    fun update(ninchatButtonViewModel: NinchatButtonViewModel) {
        ninchatButtonViewPresenter.updateViewModel(_ninchatButtonViewModel = ninchatButtonViewModel)
        ninchatButtonViewPresenter.renderCurrentView(itemView = itemView)
    }
}