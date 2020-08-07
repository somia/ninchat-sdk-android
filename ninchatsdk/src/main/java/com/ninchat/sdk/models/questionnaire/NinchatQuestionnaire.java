package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireNavigationUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireSantizer.*;

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

    public JSONObject getItem(int position) {
        if (isEmpty() || position >= size() || position < 0) {
            return null;
        }
        return getElementByIndex(questionnaireList, position);
    }

    public JSONObject getElement(int position) {
        if (isEmpty() || position >= size() || position < 0) {
            return null;
        }
        JSONObject element = getElementByIndex(questionnaireList, position);
        if (element != null) {
            return element;
        }
        // if this element if null then search for next not null element
        int nextItemIndex = getNextElementIndex(questionnaireList, position);
        return getElementByIndex(questionnaireList, nextItemIndex);
    }

    public void clear() {
        questionnaireList = null;
    }

    public JSONArray getQuestionnaireList() {
        return questionnaireList;
    }

    public void updateQuestionnaireList(JSONArray questionnaireList) {
        this.clear();
        this.questionnaireList = questionnaireList;
    }

    public void addQuestionnaire(JSONObject currentQuestionnaire) {
        this.questionnaireList.put(currentQuestionnaire);
    }

    public void removeQuestionnaireList(int at) {
        if (at >= this.size() || at < 0) return;
        this.questionnaireList.remove(at);
    }

    public JSONObject getLastElement() {
        return getItem(size() - 1);
    }

    public void removeLastElement() {
        removeQuestionnaireList(size() - 1);
    }

    public JSONObject getSecondLastElement() {
        return getItem(size() - 2);
    }

    public int updateQuestionWithThankYouElement(String thankYouText) {
        int elementIndex = this.questionnaireList.length();
        try {
            JSONArray thankYouItems = getThankYouElement(thankYouText);
            for (int i = 0; i < thankYouItems.length(); i += 1) {
                this.questionnaireList.put(thankYouItems.optJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return elementIndex;
    }
}
