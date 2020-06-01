package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatPostAudienceQuestionnaire extends NinchatQuestionnaireBase {
    private JSONArray questionnaireList;

    public NinchatPostAudienceQuestionnaire(final JSONObject configuration) {
        this.questionnaireList = this.parse(configuration, QuestionnaireType.POST_AUDIENCE_QUESTIONNAIRE);
    }

    public JSONArray getQuestionnaireList() {
        return questionnaireList;
    }
}
