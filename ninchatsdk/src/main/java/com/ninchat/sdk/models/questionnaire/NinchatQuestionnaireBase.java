package com.ninchat.sdk.models.questionnaire;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;


public class NinchatQuestionnaireBase {
    public JSONArray parse(final JSONObject configuration, final QuestionnaireType questionnaireType) {
        if (configuration == null) {
            return null;
        }
        return configuration.optJSONArray(questionnaireType.toString());
    }

    public <T> void setResult(final JSONObject element, final T result) {
        try {
            element.put("result", result);
        } catch (Exception e) {
            // pass
        }
    }

    public void setError(final JSONObject element, final boolean hasError) {
        try {
            element.put("hasError", hasError);
        } catch (Exception e) {
            // pass
        }
    }

    protected enum QuestionnaireType {
        PRE_AUDIENCE_QUESTIONNAIRE {
            @NotNull
            @Override
            public String toString() {
                return "preAudienceQuestionnaire";
            }
        },
        POST_AUDIENCE_QUESTIONNAIRE {
            @NotNull
            @Override
            public String toString() {
                return "postAudienceQuestionnaire";
            }
        },
    }
}
