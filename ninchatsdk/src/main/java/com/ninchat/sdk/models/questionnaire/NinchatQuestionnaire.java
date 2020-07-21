package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireSantizer.getThankYouElement;

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

    public void removeQuestionnaireList(final int at) {
        if (at >= this.size()) return;
        this.questionnaireList.remove(at);
    }

    public int updateQuestionWithThankYouElement(final String thankYouText, final boolean isRegister) {
        final int elementIndex = this.questionnaireList.length();
        try {
            final JSONArray thankYouItems = getThankYouElement(thankYouText, isRegister);
            for (int i = 0; i < thankYouItems.length(); i += 1) {
                this.questionnaireList.put(thankYouItems.optJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return elementIndex;
    }
}
