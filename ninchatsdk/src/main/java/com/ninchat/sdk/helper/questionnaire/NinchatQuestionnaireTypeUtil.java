package com.ninchat.sdk.helper.questionnaire;


import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatQuestionnaireTypeUtil {
    public static final int UNKNOWN = 0;
    public static final int TEXT = 1;
    public static final int INPUT = 2;
    public static final int TEXT_AREA = 3;
    public static final int SELECT = 4;
    public static final int RADIO = 5;
    public static final int LIKERT = 6;
    public static final int CHECKBOX = 7;
    public static final int OPTIONS = 8;
    public static final int BUTTON = 9;
    public static final int EOF = 10;

    public static final int PRE_AUDIENCE_QUESTIONNAIRE = 1;
    public static final int POST_AUDIENCE_QUESTIONNAIRE = 2;
    public static final int DEFAULT_INT_VALUE = -1;

    public static final int NEW_SESSION = 0;
    public static final int IN_QUEUE = 2;
    public static final int HAS_CHANNEL = 3;
    public static final int NONE = 4;

    public static boolean isText(final JSONObject jsonObject) {
        return "text".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static boolean isInput(final JSONObject jsonObject) {
        return "input".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static boolean isTextArea(final JSONObject jsonObject) {
        return "textarea".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static boolean isSelect(final JSONObject jsonObject) {
        return "select".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static boolean isRadio(final JSONObject jsonObject) {
        return "radio".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static boolean isLikeRT(final JSONObject jsonObject) {
        return "likert".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static boolean isCheckBox(final JSONObject jsonObject) {
        return "checkbox".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static boolean isButton(final JSONObject jsonObject) {
        return "buttons".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static boolean isEoF(final JSONObject jsonObject) {
        return "eof".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static boolean isSimpleFormLikeQuestionnaire(final JSONArray questionnaires) {
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

    public static boolean isGroupElement(final JSONObject element) {
        if (element == null) {
            return false;
        }
        final String elementType = element.optString("type");
        if ("group".equals(elementType) && element.has("elements")) {
            return true;
        }
        return false;
    }

    public static boolean isRegister(final String target) {
        return "_register".equalsIgnoreCase(target);
    }

    public static boolean isComplete(final String target) {
        return "_complete".equalsIgnoreCase(target);
    }


    public static boolean isRequired(final JSONObject element) {
        if (element == null) {
            return false;
        }
        return element.optBoolean("required", false);
    }

    public static boolean isElement(final JSONObject element) {
        return element != null && (element.has("elements") || element.has("element"));
    }

    public static boolean isLogic(final JSONObject element) {
        return element.has("logic");
    }

    public static int getItemType(final JSONObject jsonObject) {
        if (jsonObject == null) {
            return UNKNOWN;
        } else if (isText(jsonObject)) {
            return TEXT;
        } else if (isInput(jsonObject)) {
            return INPUT;
        } else if (isTextArea(jsonObject)) {
            return TEXT_AREA;
        } else if (isSelect(jsonObject)) {
            return SELECT;
        } else if (isRadio(jsonObject)) {
            return RADIO;
        } else if (isLikeRT(jsonObject)) {
            return LIKERT;
        } else if (isCheckBox(jsonObject)) {
            return CHECKBOX;
        } else if (isButton(jsonObject)) {
            return BUTTON;
        }
        return UNKNOWN;
    }
}
