package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.convertSimpleFormToGroup;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPostAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPreAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isConversationLikePostAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isConversationLikePreAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isConversationLikePostAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isSimpleForm;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.unifyQuestionnaire;

public class NinchatQuestionnaires {
    private NinchatQuestionnaire preAudienceQuestionnaire;
    private NinchatQuestionnaire postAudienceQuestionnaire;
    private boolean conversationLikePreAudienceQuestionnaire;
    private boolean formLikePreAudienceQuestionnaire;

    public NinchatQuestionnaires(final JSONObject configuration) {
        preAudienceQuestionnaire = new NinchatQuestionnaire(
                parse(configuration, true)
        );
        postAudienceQuestionnaire = new NinchatQuestionnaire(
                parse(configuration, false)
        );
        conversationLikePreAudienceQuestionnaire = isConversationLikePreAudienceQuestionnaire(configuration);
        formLikePreAudienceQuestionnaire = isConversationLikePostAudienceQuestionnaire(configuration);
    }

    public JSONArray parse(final JSONObject configuration, final boolean isPreAudienceQuestionnaire) {
        JSONArray questionnaireList = isPreAudienceQuestionnaire ?
                getPreAudienceQuestionnaire(configuration) : getPostAudienceQuestionnaire(configuration);

        try {
            if (isSimpleForm(questionnaireList)) {
                questionnaireList = convertSimpleFormToGroup(questionnaireList);
            }
            questionnaireList = unifyQuestionnaire(questionnaireList);
        } catch (Exception err) {
            err.printStackTrace();
        }
        return questionnaireList;
    }

    public boolean hasPreAudienceQuestionnaire() {
        return preAudienceQuestionnaire.getQuestionnaireList() != null && preAudienceQuestionnaire.getQuestionnaireList().length() > 0;
    }

    public boolean hasPostAudienceQuestionnaire() {
        return postAudienceQuestionnaire.getQuestionnaireList() != null && postAudienceQuestionnaire.getQuestionnaireList().length() > 0;
    }

    public NinchatQuestionnaire getNinchatPreAudienceQuestionnaire() {
        return preAudienceQuestionnaire;
    }

    public NinchatQuestionnaire getNinchatPostAudienceQuestionnaire() {
        return postAudienceQuestionnaire;
    }

    public boolean conversationLikePreAudienceQuestionnaire() {
        return this.conversationLikePreAudienceQuestionnaire;
    }

    public boolean formLikePreAudienceQuestionnaire() {
        return this.formLikePreAudienceQuestionnaire;
    }
}
