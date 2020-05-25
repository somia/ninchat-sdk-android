package com.ninchat.sdk.models.questionnaires;

import org.json.JSONObject;

public class NinchatPreAudienceQuestionnaires extends NinchatQuestionnairesBase{
    private JSONObject questionnaires;

    public NinchatPreAudienceQuestionnaires(final JSONObject configuration) {
        this.questionnaires = this.parse(configuration, QuestionnairesType.PRE_AUDIENCE_QUESTIONNAIRES);
    }

    public JSONObject getQuestionnaires() {
        return questionnaires;
    }
}
