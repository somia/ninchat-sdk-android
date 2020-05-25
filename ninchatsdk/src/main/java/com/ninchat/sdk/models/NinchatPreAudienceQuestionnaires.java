package com.ninchat.sdk.models;

import com.ninchat.client.JSON;

import org.json.JSONException;
import org.json.JSONObject;

public class NinchatPreAudienceQuestionnaires {
    private JSONObject questionnaires;

    public NinchatPreAudienceQuestionnaires(final JSONObject configuration) {
        this.questionnaires = this.parse(configuration);
    }

    protected JSONObject parse(final JSONObject configuration) {
        try {
            return configuration.getJSONObject("preAudienceQuestionnaire");
        } catch (JSONException e) {
            return null;
        }
    }
}
