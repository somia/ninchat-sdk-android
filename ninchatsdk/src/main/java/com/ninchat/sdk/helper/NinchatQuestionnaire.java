package com.ninchat.sdk.helper;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

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
        return element.optJSONArray("elements");
    }

    public static JSONObject getNextElement(final com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire questionnaire, final int at) {
        if (questionnaire.getQuestionnaireList() == null) {
            return null;
        }
        for (int i = at; i < questionnaire.getQuestionnaireList().length(); i += 1) {
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

    public static int getQuestionnaireElementByTarget(final com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire questionnaires, final String name) {
        if (questionnaires == null) {
            return -1;
        }
        if (name == null) {
            return -1;
        }
        for (int i = 0; i < questionnaires.size(); i += 1) {
            final JSONObject currentElement = questionnaires.getItem(i);
            if (name.equals(getName(currentElement))) {
                return i;
            }
        }
        return -1;
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

    public static String getName(final JSONObject element) {
        if (element == null) {
            return "";
        }
        return element.optString("name", null);
    }

    public static String getValue(final JSONObject element) {
        if (element == null) {
            return "";
        }
        return element.optString("value", null);
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

    public static boolean isLogic(final JSONObject element) {
        return element.has("logic");
    }

    public static JSONArray postProcess(JSONArray questionnaireList) {
        if (questionnaireList == null) {
            return null;
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

    public static String getNextTargetByRedirects(final JSONObject currentElement, final JSONArray questionnaireList) {
        final JSONArray redirects = currentElement.optJSONArray("redirects");
        if (redirects == null) {
            return null;
        }
        // only interested in first element
        final JSONObject response = questionnaireList.optJSONObject(0);
        final String value = getResultString(response);
        for (int i = 0; i < redirects.length(); i += 1) {
            final JSONObject currentRedirect = redirects.optJSONObject(i);
            if (!currentRedirect.has("pattern") || matchPattern(value, currentRedirect.optString("pattern"))) {
                return currentRedirect.optString("redirect");
            }
        }
        return null;
    }

    public static JSONArray convertSimpleFormToGroup(final JSONArray questionnaireList) throws JSONException {
        final JSONArray retval = new JSONArray();
        final JSONObject simpleForm = new JSONObject();
        simpleForm.putOpt("name", "SimpleForm");
        simpleForm.putOpt("type", "group");
        simpleForm.putOpt("buttons", new JSONObject("{\"back\":false,\"next\": \"Continue\"}"));
        simpleForm.putOpt("elements", questionnaireList);
        final JSONObject logic = new JSONObject("{\"name\":\"SimpleForm-Logic1\",\"logic\":{\"target\":\"_register\"}}");
        retval.put(simpleForm);
        retval.put(logic);
        return retval;
    }

    public static JSONObject makeGroupElement(final JSONObject nonGroupElement) throws JSONException {
        JSONArray elements = new JSONArray();
        elements.put(new JSONObject(nonGroupElement.toString()));

        JSONObject retval = new JSONObject(nonGroupElement.toString());
        retval.putOpt("elements", elements);
        retval.putOpt("type", "group");
        return retval;
    }

    public static JSONObject makeLogicElement(final JSONObject redirectElement, final String elementName, final int logicIndex) throws JSONException {
        JSONObject andLogic = new JSONObject();
        andLogic.put(elementName, redirectElement.optString("pattern"));


        JSONArray andLogicList = new JSONArray();
        andLogicList.put(andLogic);

        JSONObject logic = new JSONObject();
        logic.putOpt("target", redirectElement.optString("target"));

        // only add logic if it has pattern
        if (redirectElement.has("pattern")) {
            logic.putOpt("and", andLogicList);
        }
        JSONObject retval = new JSONObject();
        retval.putOpt("name", elementName + "-Logic" + logicIndex);
        retval.putOpt("logic", logic);
        return retval;
    }

    // make no group element to group element
    @NotNull
    public static JSONArray unifyQuestionnaire(final JSONArray questionnaireList) throws JSONException {
        if (questionnaireList == null) {
            return null;
        }
        JSONArray retval = new JSONArray();
        // convert all type of questionnaire to group questionnaire
        for (int i = 0; i < questionnaireList.length(); i += 1) {
            final JSONObject currentElement = questionnaireList.optJSONObject(i);
            if (isGroupElement(currentElement)) {
                retval.put(currentElement);
            } else if (isLogic(currentElement)) {
                retval.put(currentElement);
            } else {
                // a simple form. convert it to group element
                final JSONObject groupElement = makeGroupElement(currentElement);
                retval.put(groupElement);
            }
            final JSONArray redirectList = currentElement.optJSONArray("redirects");
            for (int j = 0; redirectList != null && j < redirectList.length(); j += 1) {
                retval.put(makeLogicElement(redirectList.optJSONObject(j), currentElement.optString("name"), j + 1));
            }
        }

        // add actions for group elements
        for (int i = 0; i < retval.length(); i += 1) {
            final JSONObject currentElement = retval.optJSONObject(i);
            if (isLogic(currentElement)) {
                continue;
            }
            final boolean hasButton = hasButton(currentElement);
            JSONArray elements = getElements(currentElement);
            if (hasButton) {
                final JSONObject tempElement = getButtonElement(currentElement);
                elements.put(tempElement);
            } else {
                for (int j = 0; j < elements.length(); j += 1) {
                    // if there is no button for this element then mark fire event true for all conditions
                    elements.optJSONObject(j).putOpt("fireEvent", true);
                }
            }
        }
        return retval;
    }

    public static JSONArray getLogicByName(final com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire questionnaire, final String name) {
        JSONArray retval = new JSONArray();
        for (int i = 0; i < questionnaire.size(); i += 1) {
            final JSONObject currentElement = questionnaire.getItem(i);
            if (!isLogic(currentElement)) {
                continue;
            }
            if (getName(currentElement).startsWith(name)) {
                retval.put(currentElement);
            }
        }
        return retval;
    }

    public static boolean isMatchedKey(final String key, final String value, final JSONArray elements) {
        final JSONObject matchedElement = getQuestionnaireElementByName(elements, key);
        if (matchedElement == null) {
            return false;
        }
        final String result = getResultString(matchedElement);
        if (result == null) {
            return false;
        }
        return matchPattern(result, value);
    }

    public static boolean isMatchedLogic(final JSONArray logicList, final JSONArray elements, boolean matchAnd) {
        if (logicList == null) {
            return false;
        }
        boolean ok = matchAnd ? true : false;
        for (int i = 0; i < logicList.length(); i += 1) {
            final JSONObject currentLogic = logicList.optJSONObject(i);
            Iterator<String> keys = currentLogic.keys();
            while (keys.hasNext()) {
                final String currentKey = keys.next();
                final String currentValue = currentLogic.optString(currentKey);
                final boolean matched = isMatchedKey(currentKey, currentValue, elements);
                ok = matchAnd ? ok & matched : ok | matched;
            }
        }
        return ok;
    }

    public static String getMatchingElement(final com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire questionnaire, final JSONObject groupQuestionnaire) {
        final JSONArray elements = getElements(groupQuestionnaire);
        final String elementName = getName(groupQuestionnaire);

        // get list of all logic that match the name
        final JSONArray logicList = getLogicByName(questionnaire, elementName);
        // go through all logic and return the index of the match logic
        for (int i = 0; i < logicList.length(); i += 1) {
            final JSONObject currentLogic = logicList.optJSONObject(i).optJSONObject("logic");
            final JSONArray andLogicList = currentLogic.optJSONArray("and");
            final JSONArray orLogicList = currentLogic.optJSONArray("or");
            if (andLogicList == null && orLogicList == null) {
                return currentLogic.optString("target");
            } else if (isMatchedLogic(andLogicList, elements, true)) {
                return currentLogic.optString("target");
            } else if (isMatchedLogic(orLogicList, elements, false)) {
                return currentLogic.optString("target");
            }
        }
        return null;
    }

    public static JSONArray getPreAudienceQuestionnaire(final JSONObject item) {
        return item.optJSONArray("preAudienceQuestionnaire");
    }

    public static JSONArray getPostAudienceQuestionnaire(final JSONObject item) {
        return item.optJSONArray("postAudienceQuestionnaire");
    }

}
