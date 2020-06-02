package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatPreAudienceQuestionnaire extends NinchatQuestionnaireBase {
    private JSONArray questionnaireList;

    public NinchatPreAudienceQuestionnaire(final JSONObject configuration) {
        this.questionnaireList = this.parse(configuration, QuestionnaireType.PRE_AUDIENCE_QUESTIONNAIRE);
    }

    public long getItemId(final int position) {
        final JSONObject currentItem = questionnaireList.optJSONObject(position);
        return currentItem.hashCode();
    }

    public JSONObject getItem(final int position) {
        if (isEmpty() || position >= size()) {
            return null;
        }
        return questionnaireList.optJSONObject(position);
    }

    public int size() {
        return questionnaireList.length();
    }

    public boolean isEmpty() {
        return questionnaireList == null || size() == 0;
    }

    public void clear() {
        questionnaireList = null;
    }

    public JSONArray getQuestionnaireList() {
        return questionnaireList;
    }
}
