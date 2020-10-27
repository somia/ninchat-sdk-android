package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference

class NinchatRadioBtnViewHolder(
        itemView: View,
        questionnaireElement: JSONObject?,
        isFormLikeQuestionnaire: Boolean,
        position: Int
) : RecyclerView.ViewHolder(itemView) {
    private val TAG = NinchatRadioBtnViewHolder::class.java.simpleName
    private val mLabel: TextView = itemView.findViewById(R.id.radio_option_label)
    private val mRecyclerViewWeakReference: WeakReference<RecyclerView> = WeakReference(itemView.findViewById(R.id.ninchat_chat_radio_options))
    private var rootElement: JSONObject? = null

    fun bind(questionnaireElement: JSONObject?, isFormLikeQuestionnaire: Boolean, position: Int, isUpdate: Boolean) {
        rootElement = questionnaireElement
        if (isFormLikeQuestionnaire) {
            itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.ninchat_chat_questionnaire_background)
        }
        val labelText = NinchatQuestionnaireItemGetter.getLabel(rootElement)
        val optionList = NinchatQuestionnaireItemGetter.getOptions(rootElement)
        mLabel.text = labelText
        mRecyclerViewWeakReference.get()!!.layoutManager = LinearLayoutManager(itemView.context)
        mRecyclerViewWeakReference.get()!!.adapter = NinchatRadioBtnAdapter(optionList)
        if (!isUpdate) NinchatQuestionnaireMiscUtil.setAnimation(itemView, position, position != 0)
    }

    inner class NinchatRadioBtnAdapter(private val optionList: JSONArray?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): RecyclerView.ViewHolder {
            val currentItem = optionList!!.optJSONObject(position)
            return NinchatRadioBtnTextViewHolder(
                    LayoutInflater.from(viewGroup.context)
                            .inflate(R.layout.radio_item, viewGroup, false), currentItem, position)
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            val currentItem = optionList!!.optJSONObject(position)
            (viewHolder as NinchatRadioBtnTextViewHolder).bind(currentItem, position)
        }

        override fun getItemCount(): Int {
            return optionList?.length() ?: 0
        }

        private fun notifyChange() {
            notifyDataSetChanged()
        }

        inner class NinchatRadioBtnTextViewHolder(itemView: View, currentItem: JSONObject, position: Int) : RecyclerView.ViewHolder(itemView) {
            private val mOption: TextView
            fun bind(currentItem: JSONObject, position: Int) {
                val label = NinchatQuestionnaireItemGetter.getLabel(currentItem)
                val currentValue = NinchatQuestionnaireItemGetter.getValue(currentItem)
                updateRadioView(NinchatQuestionnaireItemGetter.getOptionPosition(rootElement) == position)
                mOption.text = NinchatSessionManager.getInstance().ninchatState.siteConfig.getTranslation(label)
                mOption.setOnClickListener { v: View? -> onOptionClicked(currentItem, position) }
            }

            private fun updateRadioView(selected: Boolean) {
                mOption.setTextColor(
                        ContextCompat.getColor(
                                itemView.context,
                                if (selected) R.color.ninchat_color_radio_item_selected_text else R.color.ninchat_color_radio_item_unselected_text)
                )
                mOption.background = ContextCompat.getDrawable(
                        itemView.context,
                        if (selected) R.drawable.ninchat_radio_select_button else R.drawable.ninchat_ui_compose_select_button)
                val hasError = NinchatQuestionnaireItemGetter.getError(rootElement)
                if (hasError) {
                    mLabel.setTextColor(ContextCompat.getColor(
                            itemView.context, R.color.ninchat_color_error_background))
                } else {
                    mLabel.setTextColor(ContextCompat.getColor(
                            itemView.context, R.color.ninchat_colorPrimary))
                }
            }

            private fun onOptionClicked(currentItem: JSONObject, position: Int) {
                val selectedValue = NinchatQuestionnaireItemGetter.getValue(currentItem)
                val previousPosition = NinchatQuestionnaireItemGetter.getOptionPosition(rootElement)
                val isNewlySelected = position != previousPosition
                updateRadioView(isNewlySelected)

                // for options list need to select current index as well
                NinchatQuestionnaireItemSetter.setPosition(rootElement, if (isNewlySelected) position else -1)
                NinchatQuestionnaireItemSetter.setResult(rootElement, if (isNewlySelected) selectedValue else null)
                NinchatQuestionnaireItemSetter.setError(rootElement, false)
                notifyChange()
                if (isNewlySelected) {
                    mayBeFireComplete(rootElement)
                }
            }

            private fun mayBeFireComplete(rootElement: JSONObject?) {
                if (rootElement != null && rootElement.optBoolean("fireEvent", false)) {
                    EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other))
                }
            }

            init {
                mOption = itemView.findViewById(R.id.single_radio_item)
                bind(currentItem, position)
            }
        }
    }

    init {
        bind(questionnaireElement, isFormLikeQuestionnaire, position, false)
    }
}