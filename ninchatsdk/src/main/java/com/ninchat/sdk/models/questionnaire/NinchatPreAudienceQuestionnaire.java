package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.addEof;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isRequiredOK;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.matchPattern;

public class NinchatPreAudienceQuestionnaire extends NinchatQuestionnaireBase {
    private JSONArray questionnaireList;

    public NinchatPreAudienceQuestionnaire(final JSONObject configuration) {
        this.questionnaireList = this.parse(configuration, QuestionnaireType.PRE_AUDIENCE_QUESTIONNAIRE);
        addEof(questionnaireList);
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
