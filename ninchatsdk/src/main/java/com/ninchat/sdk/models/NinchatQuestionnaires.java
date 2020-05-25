package com.ninchat.sdk.models;

import org.json.JSONObject;

public class NinchatQuestionnaires {
    private NinchatPreAudienceQuestionnaires ninchatPreAudienceQuestionnaires;
    private NinchatPostAudienceQuestionnaires ninchatPostAudienceQuestionnaires;

    public NinchatQuestionnaires(final JSONObject configuration) {
        this.ninchatPreAudienceQuestionnaires = new NinchatPreAudienceQuestionnaires(configuration);
        this.ninchatPostAudienceQuestionnaires = new NinchatPostAudienceQuestionnaires(configuration);
    }

}
