package com.ninchat.sdk.models.questionnaire2;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.isRequiredOK;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.matchPattern;

public class NinchatQuestionnaire {
    private JSONArray questionnaireList;

    public NinchatQuestionnaire(JSONArray questionnaireList) {
        this.questionnaireList = questionnaireList;
    }

    public <T> void setResult(final JSONObject element, final T result) {
        try {
            element.put("result", result);
        } catch (Exception e) {
            // pass
        }
    }

    public void setError(final JSONObject element, final boolean hasError) {
        try {
            element.put("hasError", hasError);
        } catch (Exception e) {
            // pass
        }
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

    public int updateRequiredFieldStats() {
        int index = -1;
        for (int i = 0; i < questionnaireList.length(); i += 1) {
            final JSONObject item = getItem(i);
            final boolean requiredOk = isRequiredOK(item);
            final boolean patternOk = matchPattern(item);
            setError(item, !(requiredOk && patternOk));
            // take only the first item. for focusing purpose only
            if ((!requiredOk || !patternOk) && index == -1) {
                index = i;
            }
        }
        return index;
    }

}
