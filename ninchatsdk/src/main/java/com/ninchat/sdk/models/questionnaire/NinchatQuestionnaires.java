package com.ninchat.sdk.models.questionnaire;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireSantizer.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatQuestionnaires {
    private NinchatQuestionnaire preAudienceQuestionnaire;
    private NinchatQuestionnaire postAudienceQuestionnaire;
    private String audienceRegisteredText;
    private String audienceRegisteredClosedText;


    private boolean conversationLikePreAudienceQuestionnaire;
    private boolean formLikePreAudienceQuestionnaire;

    public NinchatQuestionnaires( JSONObject configuration) {
        preAudienceQuestionnaire = new NinchatQuestionnaire(
                parse(configuration, true)
        );
        postAudienceQuestionnaire = new NinchatQuestionnaire(
                parse(configuration, false)
        );
        conversationLikePreAudienceQuestionnaire = isConversationLikePreAudienceQuestionnaire(configuration);
        formLikePreAudienceQuestionnaire = isConversationLikePostAudienceQuestionnaire(configuration);
        audienceRegisteredText = getAudienceRegisteredTextFromConfig(configuration);
        audienceRegisteredClosedText = getAudienceRegisteredClosedTextFromConfig(configuration);
    }

    public JSONArray parse( JSONObject configuration,  boolean isPreAudienceQuestionnaire) {
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

    public boolean formLikePreAudienceQuestionnaire() {
        return this.formLikePreAudienceQuestionnaire;
    }
}
