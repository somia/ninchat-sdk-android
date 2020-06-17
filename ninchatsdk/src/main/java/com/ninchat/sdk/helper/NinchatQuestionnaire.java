package com.ninchat.sdk.helper;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;


import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatQuestionnaire {
    public static final int UNKNOWN = 0;
    public static final int TEXT = 1;
    public static final int INPUT = 2;
    public static final int TEXT_AREA = 3;
    public static final int SELECT = 4;
    public static final int RADIO = 5;
    public static final int LIKERT = 6;
    public static final int CHECKBOX = 7;
    public static final int OPTIONS = 8;
    public static final int EOF = 9;

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

    public static boolean isEoF(final JSONObject jsonObject) {
        return "eof".equalsIgnoreCase(jsonObject.optString("element"));
    }

    public static int getItemType(final JSONObject jsonObject) {
        if (jsonObject == null) {
            return NinchatQuestionnaire.UNKNOWN;
        } else if (NinchatQuestionnaire.isText(jsonObject)) {
            return NinchatQuestionnaire.TEXT;
        } else if (NinchatQuestionnaire.isInput(jsonObject)) {
            return NinchatQuestionnaire.INPUT;
        } else if (NinchatQuestionnaire.isTextArea(jsonObject)) {
            return NinchatQuestionnaire.TEXT_AREA;
        } else if (NinchatQuestionnaire.isSelect(jsonObject)) {
            return NinchatQuestionnaire.SELECT;
        } else if (NinchatQuestionnaire.isRadio(jsonObject)) {
            return NinchatQuestionnaire.RADIO;
        } else if (NinchatQuestionnaire.isLikeRT(jsonObject)) {
            return NinchatQuestionnaire.LIKERT;
        } else if (NinchatQuestionnaire.isCheckBox(jsonObject)) {
            return NinchatQuestionnaire.CHECKBOX;
        } else if (NinchatQuestionnaire.isEoF(jsonObject)) {
            return NinchatQuestionnaire.EOF;
        }
        return NinchatQuestionnaire.UNKNOWN;
    }

    public static Spanned fromHTML(String source) {
        return source == null ? null : Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(source);
    }

    public static boolean isSimpleForm(final JSONArray questionnaires) {
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

    public static JSONObject getQuestionnaireElementByName(final JSONArray questionnaires, final String name) {
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


    public static String getPattern(final JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optString("pattern", null);
    }

    public static boolean isRequired(final JSONObject element) {
        if (element == null) {
            return false;
        }
        return element.optBoolean("required", false);
    }

    public static String getLabel(final JSONObject element) {
        if (element == null) {
            return "";
        }
        return element.optString("label", null);
    }


    public static JSONArray getOptions(final JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optJSONArray("options");
    }


    public static String getResultString(final JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optString("result", null);
    }

    public static int getResultInt(final JSONObject element) {
        if (element == null) {
            return -1;
        }
        return element.optInt("result", -1);
    }

    public static boolean getResultBoolean(final JSONObject element) {
        if (element == null) {
            return false;
        }
        return element.optBoolean("result", false);
    }


    public static boolean getError(final JSONObject element) {
        if (element == null) {
            return false;
        }
        return element.optBoolean("hasError", false);
    }


    public static boolean matchPattern(final String currentInput, final String pattern) {
        // no pattern given. so everything is valid
        if (TextUtils.isEmpty(pattern)) {
            return true;
        }
        return (currentInput == null ? "" : currentInput).matches(pattern);
    }

    public static boolean matchPattern(final JSONObject element) {
        if (element == null) {
            return false;
        }
        final String pattern = getPattern(element);
        if (TextUtils.isEmpty(pattern)) {
            return true;
        }
        if (isInput(element) || isTextArea(element)) {
            final String value = getResultString(element);
            return matchPattern(value, pattern);
        }
        return true;
    }


    public static boolean hasResult(final JSONObject element) {
        if ((isInput(element) || isTextArea(element))) {
            final String value = getResultString(element);
            return !TextUtils.isEmpty(value);
        }
        if ((isSelect(element) || isLikeRT(element))) {
            final int value = getResultInt(element);
            return value > 0;
        }
        if (isRadio(element)) {
            final int value = getResultInt(element);
            return value >= 0;
        }
        if (isCheckBox(element)) {
            return getResultBoolean(element);
        }
        return true;
    }


    public static boolean isRequiredOK(final JSONObject element) {
        final boolean isRequired = isRequired(element);
        // if not an required field. then good
        if (!isRequired) {
            return true;
        }
        return hasResult(element);
    }

    public static void addEof(JSONArray itemList) {
        if (itemList == null) {
            return;
        }
        final String jsonString = "{\"element\":\"eof\"}";
        try {
            itemList.put(new JSONObject(jsonString));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
