package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.isInput;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isTextArea;

public class NinchatPreAudienceQuestionnaire extends NinchatQuestionnaireBase {
    private JSONArray questionnaireList;

    public NinchatPreAudienceQuestionnaire(final JSONObject configuration) {
        this.questionnaireList = this.parse(configuration, QuestionnaireType.PRE_AUDIENCE_QUESTIONNAIRE);
        this.addEOF();
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

    private void addEOF() {
        final String jsonString = "{\"element\":\"eof\"}";
        try {
            questionnaireList.put(new JSONObject(jsonString));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public int checkRequiredFields() {
        int index = -1;
        for (int i = 0; i < questionnaireList.length(); i += 1) {
            final JSONObject item = getItem(i);
            boolean isValidInput = true;
            if (isTextArea(item) || isInput(item)) {
                // check for additional pattern match
                final String text = getResultString(item);
                final String pattern = getPattern(item);
                isValidInput = isValidInput(text, pattern);
            }
            final boolean hasError = hasError(item);
            setError(item, hasError || !isValidInput);
            if ((hasError || !isValidInput) && index == -1) {
                index = i;
            }
        }
        return index;
    }
}
