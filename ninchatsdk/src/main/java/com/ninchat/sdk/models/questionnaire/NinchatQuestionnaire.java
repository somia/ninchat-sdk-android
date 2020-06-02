package com.ninchat.sdk.models.questionnaire;

import org.json.JSONObject;

public class NinchatQuestionnaire {
    private NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire;
    private NinchatPostAudienceQuestionnaire ninchatPostAudienceQuestionnaire;

    public NinchatQuestionnaire(final JSONObject configuration) {
        this.ninchatPreAudienceQuestionnaire = new NinchatPreAudienceQuestionnaire(configuration);
        this.ninchatPostAudienceQuestionnaire = new NinchatPostAudienceQuestionnaire(configuration);
    }

    public boolean hasPreAudienceQuestionnaire() {
        return this.ninchatPreAudienceQuestionnaire.getQuestionnaireList() != null;
    }

    public boolean hasPostAudienceQuestionnaire() {
        return this.ninchatPostAudienceQuestionnaire.getQuestionnaireList() != null;
    }

    public NinchatPreAudienceQuestionnaire getNinchatPreAudienceQuestionnaire() {
        return ninchatPreAudienceQuestionnaire;
    }

    public NinchatPostAudienceQuestionnaire getNinchatPostAudienceQuestionnaire() {
        return ninchatPostAudienceQuestionnaire;
    }
}
