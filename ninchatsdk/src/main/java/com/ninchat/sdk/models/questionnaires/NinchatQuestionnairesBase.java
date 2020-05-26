package com.ninchat.sdk.models.questionnaires;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NinchatQuestionnairesBase {
    protected JSONArray parse(final JSONObject configuration, final QuestionnairesType questionnairesType) {
        if (configuration == null) {
            return null;
        }
        return configuration.optJSONArray(questionnairesType.toString());
    }

    protected boolean simpleForm(final JSONArray questionnaire) {
        if (questionnaire == null) {
            return false;
        }
        for (int i = 0; i < questionnaire.length(); i += 1) {
            final JSONObject currentElement = questionnaire.optJSONObject(i);
            if (currentElement == null) {
                continue;
            }
            final JSONArray redirects = currentElement.optJSONArray("redirects");
            final JSONObject logic = currentElement.optJSONObject("logic");
            final JSONObject buttons = currentElement.optJSONObject("buttons");
            final String elementType = currentElement.optString("type");

            if (redirects != null || logic != null || buttons != null || elementType.equals("group")) {
                return false;
            }
        }
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
