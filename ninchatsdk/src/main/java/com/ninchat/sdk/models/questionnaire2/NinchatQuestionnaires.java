package com.ninchat.sdk.models.questionnaire2;

import com.ninchat.sdk.models.questionnaire.NinchatPostAudienceQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPostAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPreAudienceQuestionnaire;

public class NinchatQuestionnaires {
    private NinchatQuestionnaire preAudienceQuestionnaire;
    private NinchatQuestionnaire postAudienceQuestionnaire;

    public NinchatQuestionnaires(final JSONObject configuration) {
        preAudienceQuestionnaire = new NinchatQuestionnaire(getPreAudienceQuestionnaire(configuration));
        postAudienceQuestionnaire = new NinchatQuestionnaire(getPostAudienceQuestionnaire(configuration));
    }

    public boolean hasPreAudienceQuestionnaire() {
        return preAudienceQuestionnaire.getQuestionnaireList() != null;
    }

    public boolean hasPostAudienceQuestionnaire() {
        return postAudienceQuestionnaire.getQuestionnaireList() != null;
    }

    public NinchatQuestionnaire getNinchatPreAudienceQuestionnaire() {
        return preAudienceQuestionnaire;
    }

    public NinchatQuestionnaire getNinchatPostAudienceQuestionnaire() {
        return postAudienceQuestionnaire;
    }
}
