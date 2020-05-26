package com.ninchat.sdk.models.questionnaires;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatPreAudienceQuestionnaires extends NinchatQuestionnairesBase{
    private JSONArray questionnaires;

    public NinchatPreAudienceQuestionnaires(final JSONObject configuration) {
        this.questionnaires = this.parse(configuration, QuestionnairesType.PRE_AUDIENCE_QUESTIONNAIRES);
    }

    public JSONArray getQuestionnaires() {
        return questionnaires;
    }
}
