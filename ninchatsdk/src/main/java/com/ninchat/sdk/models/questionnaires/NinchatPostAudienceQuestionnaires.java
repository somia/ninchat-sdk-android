package com.ninchat.sdk.models.questionnaires;

import org.json.JSONObject;

public class NinchatPostAudienceQuestionnaires extends NinchatQuestionnairesBase{
    private JSONObject questionnaires;

    public NinchatPostAudienceQuestionnaires(final JSONObject configuration) {
        this.questionnaires = this.parse(configuration, QuestionnairesType.POST_AUDIENCE_QUESTIONNAIRES);
    }

    public JSONObject getQuestionnaires() {
        return questionnaires;
    }
}
