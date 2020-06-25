package com.ninchat.sdk.helper;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;


import org.json.JSONArray;
import org.json.JSONException;
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
    public static final int BUTTON = 9;
    public static final int EOF = 10;

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
        } else if (NinchatQuestionnaire.isButton(jsonObject)) {
            return NinchatQuestionnaire.BUTTON;
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

    public static JSONArray getElements(final JSONObject element) {
        if (element == null) {
            return null;
        }
        if (element.has("elements")) {
            try {
                return new JSONArray(element.optJSONArray("elements").toString());
            } catch (JSONException e) {
                return null;
            }
        }
        JSONArray retval = new JSONArray();
        try {
            retval.put(new JSONObject(element.toString()));
        } catch (JSONException e) {
            return retval;
        }
        return retval;
    }

    public static JSONObject getNextElement(final com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire questionnaire, final int lastIndex) {
        if (questionnaire.getQuestionnaireList() == null) {
            return null;
        }
        for (int i = lastIndex + 1; i < questionnaire.getQuestionnaireList().length(); i += 1) {
            final JSONObject currentElement = questionnaire.getQuestionnaireList().optJSONObject(i);
            if (isElement(currentElement)) {
                return currentElement;
            }
        }
        return null;
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

    public static boolean hasButton(final JSONObject buttonElement, final boolean isBack) {
        if (buttonElement == null) {
            return false;
        }
        return "false".compareToIgnoreCase(buttonElement.optString(isBack ? "back" : "next")) != 0;
    }

    public static boolean hasButton(final JSONObject element) {
        final JSONObject buttons = element.optJSONObject("buttons");
        if (buttons == null) {
            return false;
        }
        final boolean hasBack = hasButton(buttons, true);
        final boolean hasNext = hasButton(buttons, false);
        return (hasBack || hasNext);
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

    public static boolean isElement(final JSONObject element) {
        return element.has("elements") || element.has("element");
    }

    public static JSONArray postProcess(JSONArray questionnaireList) {
        if (questionnaireList == null) {
            return null;
        }
        // if simple questionnaire then check if last element has a button. If it doesnot then add a button element with next enabled
        if (isSimpleForm(questionnaireList)) {
            // todo(pallab) do same think for simple form as well so that we can use same view and recycler for both simple and complex form
            return questionnaireList;
        }

        try {
            // at this point it is a complex questionnaire with either logic or redirect
            for (int i = 0; i < questionnaireList.length(); i += 1) {
                JSONObject currentElement = questionnaireList.optJSONObject(i);
                if (!isElement(currentElement)) {
                    continue;
                }
                JSONArray elements = getElements(currentElement);
                final boolean hasButton = hasButton(currentElement);
                if (hasButton) {
                    final JSONObject tempElement = getButtonElement(currentElement);
                    elements.put(tempElement);
                }
                for (int j = 0; j < elements.length(); j += 1) {
                    // if there is no button for this element then mark fire event true for all conditions
                    if (!hasButton) {
                        elements.optJSONObject(j).putOpt("fireEvent", true);
                    }
                }
                currentElement.put("elements", elements);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questionnaireList;
    }

    public static JSONObject getButtonElement(final JSONObject element) {
        JSONObject retval = new JSONObject();
        try {
            final JSONObject buttons = element.optJSONObject("buttons");
            retval.putOpt("element", "buttons");
            retval.putOpt("fireEvent", true);
            retval.putOpt("back", hasButton(buttons, true) ? buttons.optString("back") : false);
            retval.putOpt("next", hasButton(buttons, false) ? buttons.optString("next") : false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }

    public static JSONArray getPreAudienceQuestionnaire(final JSONObject item) {
        return item.optJSONArray("preAudienceQuestionnaire");
    }

    public static JSONArray getPostAudienceQuestionnaire(final JSONObject item) {
        return item.optJSONArray("postAudienceQuestionnaire");
    }

}
