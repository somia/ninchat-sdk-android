package com.ninchat.sdk.models.questionnaires;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatQuestionnairesBase {
    protected JSONArray  parse(final JSONObject configuration, final QuestionnairesType questionnairesType) {
        try {
            return configuration.getJSONArray(questionnairesType.toString());
        } catch (Exception e) {
            return null;
        }
    }

    protected boolean simpleForm(final JSONObject questionnaire) {
        return true;
    }

    protected enum QuestionnairesType {
        PRE_AUDIENCE_QUESTIONNAIRES {
            @NotNull
            @Override
            public String toString() {
                return "preAudienceQuestionnaire";
            }
        },
        POST_AUDIENCE_QUESTIONNAIRES {
            @NotNull
            @Override
            public String toString() {
                return "postAudienceQuestionnaire";
            }
        },
    }
}
