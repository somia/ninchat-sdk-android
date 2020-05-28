package com.ninchat.sdk.models.questionnaires;

import org.json.JSONObject;

public class NinchatQuestionnaires {
    private NinchatPreAudienceQuestionnaires ninchatPreAudienceQuestionnaires;
    private NinchatPostAudienceQuestionnaires ninchatPostAudienceQuestionnaires;

    public NinchatQuestionnaires(final JSONObject configuration) {
        this.ninchatPreAudienceQuestionnaires = new NinchatPreAudienceQuestionnaires(configuration);
        this.ninchatPostAudienceQuestionnaires = new NinchatPostAudienceQuestionnaires(configuration);
    }

    public boolean hasPreAudienceQuestionnaires() {
        return this.ninchatPreAudienceQuestionnaires.getQuestionnaires() != null;
    }

    public boolean hasPostAudienceQuestionnaires() {
        return this.ninchatPostAudienceQuestionnaires.getQuestionnaires() != null;
    }

}
