package com.ninchat.sdk.models;

import org.json.JSONException;
import org.json.JSONObject;

public class NinchatPostAudienceQuestionnaires {
    private JSONObject questionnaires;

    public NinchatPostAudienceQuestionnaires(final JSONObject configuration) {
        this.questionnaires = this.parse(configuration);
    }

    protected JSONObject parse(final JSONObject configuration) {
        try {
            return configuration.getJSONObject("postAudienceQuestionnaire");
        } catch (JSONException e) {
            return null;
        }
    }
}
