package com.ninchat.sdk.models.questionnaire2;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPostAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPreAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.postProcess;

public class NinchatQuestionnaires {
    private NinchatQuestionnaire preAudienceQuestionnaire;
    private NinchatQuestionnaire postAudienceQuestionnaire;

    public NinchatQuestionnaires(final JSONObject configuration) {
        preAudienceQuestionnaire = new NinchatQuestionnaire(
                postProcess(getPreAudienceQuestionnaire(configuration))
        );
        postAudienceQuestionnaire = new NinchatQuestionnaire(
                postProcess(getPostAudienceQuestionnaire(configuration))
        );
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
