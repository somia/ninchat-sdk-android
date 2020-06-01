package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatPreAudienceQuestionnaire extends NinchatQuestionnaireBase {
    private JSONArray questionnaireList;

    public NinchatPreAudienceQuestionnaire(final JSONObject configuration) {
        this.questionnaireList = this.parse(configuration, QuestionnaireType.PRE_AUDIENCE_QUESTIONNAIRE);
    }

    public JSONArray getQuestionnaireList() {
        return questionnaireList;
    }
}
