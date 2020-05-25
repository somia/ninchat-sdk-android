package com.ninchat.sdk.models.questionnaires;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class NinchatQuestionnairesBase {
    protected JSONObject parse(final JSONObject configuration, final QuestionnairesType questionnairesType) {
        try {
            return configuration.getJSONObject(questionnairesType.toString());
        } catch (Exception e) {
            return null;
        }
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
