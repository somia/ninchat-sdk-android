package com.ninchat.sdk.models.questionnaire;

import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatQuestionnaireBase {
    protected JSONArray parse(final JSONObject configuration, final QuestionnaireType questionnaireType) {
        if (configuration == null) {
            return null;
        }
        return configuration.optJSONArray(questionnaireType.toString());
    }

    protected boolean simpleForm(final JSONArray questionnaires) {
        if (questionnaires == null) {
            return false;
        }
        for (int i = 0; i < questionnaires.length(); i += 1) {
            final JSONObject currentElement = questionnaires.optJSONObject(i);
            final JSONArray redirects = currentElement.optJSONArray("redirects");
            final JSONObject logic = currentElement.optJSONObject("logic");
            final JSONObject buttons = currentElement.optJSONObject("buttons");
            final String elementType = currentElement.optString("type");

            if (redirects != null || logic != null || buttons != null || "group".equals(elementType)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isGroupElement(final JSONObject element) {
        if (element == null) {
            return false;
        }
        final String elementType = element.optString("type");
        if ("group".equals(elementType) && element.has("elements")) {
            return true;
        }
        return false;
    }

    protected JSONObject getQuestionnaireElementByName(final JSONArray questionnaires, final String name) {
        if (questionnaires == null) {
            return null;
        }
        if (name == null) {
            return null;
        }
        for (int i = 0; i < questionnaires.length(); i += 1) {
            final JSONObject currentElement = questionnaires.optJSONObject(i);
            if (name.equals(currentElement.optString("name"))) {
                return currentElement;
            }
        }
        return null;
    }

    public String getPattern(final JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optString("pattern");
    }

    public boolean isValidInput(final String currentInput, final String pattern) {
        // no pattern given. so everything is valid
        if (TextUtils.isEmpty(pattern)) {
            return true;
        }
        if (TextUtils.isEmpty(currentInput)) {
            return true;
        }
        return currentInput.matches(pattern);
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
