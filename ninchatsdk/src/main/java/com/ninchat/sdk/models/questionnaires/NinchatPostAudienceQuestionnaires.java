package com.ninchat.sdk.models.questionnaires;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatPostAudienceQuestionnaires extends NinchatQuestionnairesBase{
    private JSONArray questionnaires;

    public NinchatPostAudienceQuestionnaires(final JSONObject configuration) {
        this.questionnaires = this.parse(configuration, QuestionnairesType.POST_AUDIENCE_QUESTIONNAIRES);
    }

    public JSONArray getQuestionnaires() {
        return questionnaires;
    }
}
