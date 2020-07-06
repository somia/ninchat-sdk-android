package com.ninchat.sdk.models.questionnaire2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.convertSimpleFormToGroup;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPostAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPreAudienceQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isSimpleForm;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.unifyQuestionnaire;

public class NinchatQuestionnaires {
    private NinchatQuestionnaire preAudienceQuestionnaire;
    private NinchatQuestionnaire postAudienceQuestionnaire;

    public NinchatQuestionnaires(final JSONObject configuration) {
        preAudienceQuestionnaire = new NinchatQuestionnaire(
                parse(configuration, true)
        );
        postAudienceQuestionnaire = new NinchatQuestionnaire(
                parse(configuration, false)
        );
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
        return preAudienceQuestionnaire.getQuestionnaireList() != null && preAudienceQuestionnaire.getQuestionnaireList().length() > 0 ;
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
}
