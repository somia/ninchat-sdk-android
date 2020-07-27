package com.ninchat.sdk.models.questionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireSantizer.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatQuestionnaireHolder {
    private NinchatQuestionnaire preAudienceQuestionnaire;
    private NinchatQuestionnaire postAudienceQuestionnaire;
    private String audienceRegisteredText;
    private String audienceRegisteredClosedText;


    private boolean conversationLikePreAudienceQuestionnaire;
    private boolean conversationLikePostAudienceQuestionnaire;

    public NinchatQuestionnaireHolder(JSONObject configuration) {
        preAudienceQuestionnaire = new NinchatQuestionnaire(
                parseQuestionnaire(configuration, true)
        );
        postAudienceQuestionnaire = new NinchatQuestionnaire(
                parseQuestionnaire(configuration, false)
        );
        conversationLikePreAudienceQuestionnaire = isConversationLikePreAudienceQuestionnaire(configuration);
        conversationLikePostAudienceQuestionnaire = isConversationLikePostAudienceQuestionnaire(configuration);
        audienceRegisteredText = getAudienceRegisteredTextFromConfig(configuration);
        audienceRegisteredClosedText = getAudienceRegisteredClosedTextFromConfig(configuration);
    }

    public JSONArray parseQuestionnaire(JSONObject configuration, boolean isPreAudienceQuestionnaire) {
        JSONArray questionnaireList = isPreAudienceQuestionnaire ?
                getPreAudienceQuestionnaire(configuration) : getPostAudienceQuestionnaire(configuration);

        try {
            if (isSimpleFormLikeQuestionnaire(questionnaireList)) {
                questionnaireList = convertSimpleFormToGroupQuestionnaire(questionnaireList);
            }
            questionnaireList = unifyQuestionnaireList(questionnaireList);
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

    public String getAudienceRegisteredText() {
        return audienceRegisteredText;
    }

    public String getAudienceRegisteredClosedText() {
        return audienceRegisteredClosedText;
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

    public boolean conversationLikePostAudienceQuestionnaire() {
        return this.conversationLikePostAudienceQuestionnaire;
    }
}
