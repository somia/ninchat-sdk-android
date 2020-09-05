package com.ninchat.sdk.models.questionnaire;

import com.ninchat.sdk.NinchatSessionManager;
import org.json.JSONArray;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireSantizer.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatQuestionnaireHolder {
    private NinchatQuestionnaire preAudienceQuestionnaire;
    private NinchatQuestionnaire postAudienceQuestionnaire;
    private String audienceRegisteredText;
    private String audienceRegisteredClosedText;
    private String botQuestionnaireName;
    private String botQuestionnaireAvatar;


    private boolean conversationLikePreAudienceQuestionnaire;
    private boolean conversationLikePostAudienceQuestionnaire;

    public NinchatQuestionnaireHolder(NinchatSessionManager ninchatSessionManager) {
        preAudienceQuestionnaire = new NinchatQuestionnaire(
                parseQuestionnaire(ninchatSessionManager, true)
        );
        postAudienceQuestionnaire = new NinchatQuestionnaire(
                parseQuestionnaire(ninchatSessionManager, false)
        );
        conversationLikePreAudienceQuestionnaire = isConversationLikePreAudienceQuestionnaire(ninchatSessionManager);
        conversationLikePostAudienceQuestionnaire = isConversationLikePostAudienceQuestionnaire(ninchatSessionManager);
        audienceRegisteredText = getAudienceRegisteredTextFromConfig(ninchatSessionManager);
        audienceRegisteredClosedText = getAudienceRegisteredClosedTextFromConfig(ninchatSessionManager);
        botQuestionnaireName = getBotQuestionnaireNameFromConfig(ninchatSessionManager);
        botQuestionnaireAvatar = getBotQuestionnaireAvatarFromConfig(ninchatSessionManager);
    }

    public JSONArray parseQuestionnaire(NinchatSessionManager ninchatSessionManager, boolean isPreAudienceQuestionnaire) {
        JSONArray questionnaireList = isPreAudienceQuestionnaire ?
                getPreAudienceQuestionnaire(ninchatSessionManager) : getPostAudienceQuestionnaire(ninchatSessionManager);

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

    public String getBotQuestionnaireName(){
        return botQuestionnaireName;
    }

    public String getBotQuestionnaireAvatar(){
        return botQuestionnaireAvatar;
    }
}
