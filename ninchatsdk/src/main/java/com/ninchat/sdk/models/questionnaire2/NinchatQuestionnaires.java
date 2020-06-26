package com.ninchat.sdk.models.questionnaire2;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPostAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPreAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.unifyQuestionnaire;

public class NinchatQuestionnaires {
    private NinchatQuestionnaire preAudienceQuestionnaire;
    private NinchatQuestionnaire postAudienceQuestionnaire;

    public NinchatQuestionnaires(final JSONObject configuration) {
        try {
            preAudienceQuestionnaire = new NinchatQuestionnaire(
                    unifyQuestionnaire(getPreAudienceQuestionnaire(configuration))
            );
            postAudienceQuestionnaire = new NinchatQuestionnaire(
                    unifyQuestionnaire(getPostAudienceQuestionnaire(configuration))
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
