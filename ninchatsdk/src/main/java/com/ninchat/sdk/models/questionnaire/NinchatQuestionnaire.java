package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatQuestionnaire {
    private JSONArray questionnaireList;

    public NinchatQuestionnaire(JSONArray questionnaireList) {
        this.questionnaireList = questionnaireList;
    }


    public int size() {
        return questionnaireList == null ? 0 : questionnaireList.length();
    }

    public boolean isEmpty() {
        return questionnaireList == null || size() == 0;
    }

    public JSONObject getItem(final int position) {
        if (isEmpty() || position >= size()) {
            return null;
        }
        return questionnaireList.optJSONObject(position);
    }

    public void clear() {
        questionnaireList = null;
    }

    public JSONArray getQuestionnaireList() {
        return questionnaireList;
    }

    public void updateQuestionnaireList(final JSONArray questionnaireList) {
        this.clear();
        this.questionnaireList = questionnaireList;
    }

    public void addQuestionnaireList(final JSONObject currentQuestionnaire) {
        this.questionnaireList.put(currentQuestionnaire);
    }
}
