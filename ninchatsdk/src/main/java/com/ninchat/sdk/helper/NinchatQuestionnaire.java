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
import java.util.Stack;

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

    public static boolean isRegister(final String target) {
        return "_register".equalsIgnoreCase(target);
    }

    public static boolean isComplete(final String target) {
        return "_complete".equalsIgnoreCase(target);
    }

    public static JSONObject getCurrentElement(final JSONArray questionnaire, final int at) {
        if (questionnaire == null || at < 0 || at >= questionnaire.length()) {
            return null;
        }
        if (!isElement(questionnaire.optJSONObject(at))) {
            return null;
        }
        return questionnaire.optJSONObject(at);
    }

    public static int getNextElementIndex(final JSONArray questionnaire, final int at) {
        if (questionnaire == null || at < 0 || at >= questionnaire.length()) {
            return -1;
        }
        for (int i = at + 1; i < questionnaire.length(); i += 1) {
            if (isElement(questionnaire.optJSONObject(i))) {
                return i;
            }
        }
        return -1;
    }

    public static JSONObject getQuestionnaireElementByName(final JSONArray questionnaireList, final String name) {
        if (questionnaireList == null) {
            return null;
        }
        if (name == null) {
            return null;
        }
        for (int i = 0; i < questionnaireList.length(); i += 1) {
            final JSONObject currentElement = questionnaireList.optJSONObject(i);
            if (name.equals(currentElement.optString("name"))) {
                return currentElement;
            }
        }
        return null;
    }

    public static int getQuestionnaireElementIndexByName(final JSONArray questionnaireList, final String name) {
        if (name == null) {
            return -1;
        }
        for (int i = 0; questionnaireList != null && i < questionnaireList.length(); i += 1) {
            final JSONObject currentElement = questionnaireList.optJSONObject(i);
            if (currentElement != null && name.equals(getName(currentElement))) {
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
        return (currentInput == null ? "" : currentInput).matches(pattern == null ? "" : pattern);
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
        return element != null && (element.has("elements") || element.has("element"));
    }

    public static boolean isLogic(final JSONObject element) {
        return element.has("logic");
    }

    public static JSONObject getButtonElement(final JSONObject element, boolean hideBack) {
        JSONObject retval = new JSONObject();
        try {
            final JSONObject buttons = element.optJSONObject("buttons");
            retval.putOpt("element", "buttons");
            retval.putOpt("fireEvent", true);
            retval.putOpt("back", hideBack ? false : hasButton(buttons, true) ? buttons.optString("back") : false);
            retval.putOpt("next", hasButton(buttons, false) ? buttons.optString("next") : false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }

    public static JSONObject getButtonElement(boolean hideBack) {
        JSONObject retval = new JSONObject();
        try {
            retval.putOpt("element", "buttons");
            retval.putOpt("fireEvent", true);
            retval.putOpt("back", hideBack ? false : true);
            retval.putOpt("next", "Continue");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
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

    public static JSONArray convertRedirectsToLogicList(final JSONObject elements, final int index) throws JSONException {
        final JSONArray redirectList = elements.optJSONArray("redirects");
        final String elementName = elements.optString("name");
        JSONArray retval = new JSONArray();
        for (int i = 0; redirectList != null && i < redirectList.length(); i += 1) {
            final JSONObject redirectElement = redirectList.optJSONObject(i);
            if (redirectElement == null) continue;
            final JSONObject logicElement = makeLogicElement(redirectElement, elementName, index);
            retval.put(logicElement);
        }
        return retval;
    }

    public static JSONArray updateActions(final JSONArray questionnaireList) throws JSONException {
        for (int i = 0; questionnaireList != null && i < questionnaireList.length(); i += 1) {
            final JSONObject currentElement = questionnaireList.optJSONObject(i);
            if (currentElement == null) continue;
            if (isLogic(currentElement)) continue;

            final JSONArray elementList = getElements(currentElement);
            if (elementList == null || elementList.length() == 0) continue;
            final boolean hasButton = hasButton(currentElement);
            if (hasButton) {
                final JSONObject tempElement = getButtonElement(currentElement, i == 0);
                elementList.put(tempElement);
            } else {
                // add event fire capability to last element if it is not an text, input, or
                final JSONObject tempElement = elementList.optJSONObject(elementList.length() - 1);
                if (tempElement == null) continue;
                if (isText(tempElement) || isInput(tempElement) || isTextArea(tempElement)) {
                    final JSONObject tempBtnElement = getButtonElement(true);
                    elementList.put(tempBtnElement);
                } else {
                    // if there is no button for this element then mark fire event true for all conditions
                    // if the last element if not text input or text area
                    tempElement.putOpt("fireEvent", true);
                }
            }
        }
        return questionnaireList;
    }

    @NotNull
    public static JSONArray unifyQuestionnaire(final JSONArray questionnaireList) throws JSONException {
        JSONArray retval = new JSONArray();
        // convert all type of questionnaire to group questionnaire
        for (int i = 0; questionnaireList != null && i < questionnaireList.length(); i += 1) {
            final JSONObject currentElement = questionnaireList.optJSONObject(i);
            if (currentElement == null) continue;
            if (isGroupElement(currentElement)) {
                retval.put(currentElement);
            } else if (isLogic(currentElement)) {
                retval.put(currentElement);
            } else {
                // a simple form. convert it to group element
                final JSONObject groupElement = makeGroupElement(currentElement);
                retval.put(groupElement);
            }
            final JSONArray redirectList = convertRedirectsToLogicList(currentElement, i);
            for (int j = 0; redirectList != null && j < redirectList.length(); j += 1) {
                retval.put(redirectList.optJSONObject(j));
            }
        }
        // add actions for group elements
        return updateActions(retval);
    }

    public static JSONArray getLogicByName(final JSONArray questionnaireList, final String name) {
        JSONArray retval = new JSONArray();
        if (TextUtils.isEmpty(name)) {
            return retval;
        }
        for (int i = 0; questionnaireList != null && i < questionnaireList.length(); i += 1) {
            final JSONObject currentElement = questionnaireList.optJSONObject(i);
            if (currentElement == null) continue;
            if (!isLogic(currentElement)) {
                continue;
            }
            if (getName(currentElement).startsWith(name)) {
                retval.put(currentElement);
            } else if (getName(currentElement).startsWith("Logic-" + name)) {
                retval.put(currentElement);
            }
        }
        return retval;
    }


    public static boolean matchedLogicCore(final String key, final String value, final JSONArray elements) {
        final JSONObject matchedElement = getQuestionnaireElementByName(elements, key);
        if (matchedElement == null) {
            return false;
        }
        final String result = getResultString(matchedElement);
        return matchPattern(result, value);
    }

    public static boolean matchedLogic(final JSONArray logicList, final JSONArray elements, boolean matchAnd) {
        if (logicList == null) {
            return false;
        }
        boolean ok = matchAnd ? true : false;
        for (int i = 0; i < logicList.length(); i += 1) {
            final JSONObject currentLogic = logicList.optJSONObject(i);
            Iterator<String> keys = currentLogic.keys();
            while (keys.hasNext()) {
                final String currentKey = keys.next();
                // if current key is empty, then just ignore and continue
                if (TextUtils.isEmpty(currentKey)) {
                    continue;
                }
                final String currentValue = currentLogic.optString(currentKey);
                final boolean matched = matchedLogicCore(currentKey, currentValue, elements);
                ok = matchAnd ? ok & matched : ok | matched;
            }
        }
        return ok;
    }

    public static boolean hasLogic(final JSONObject logicElement, final boolean isAnd) {
        if (logicElement == null) {
            return false;
        }
        final JSONArray logicList = logicElement.optJSONArray(isAnd ? "and" : "or");
        for (int i = 0; logicList != null && i < logicList.length(); i += 1) {
            final JSONObject currentLogic = logicList.optJSONObject(i);
            if (currentLogic == null) continue;
            Iterator<String> keys = currentLogic.keys();
            while (keys.hasNext()) {
                final String currentKey = keys.next();
                // if current key is not empty
                if (!TextUtils.isEmpty(currentKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static JSONArray getMatchingLogic(final JSONArray questionnaireList, final JSONObject groupQuestionnaire) {
        final JSONArray elementList = getElements(groupQuestionnaire);
        JSONArray logicList = getLogicByName(questionnaireList, getName(groupQuestionnaire));
        for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
            final JSONObject currentElement = elementList.optJSONObject(i);
            if (currentElement == null) continue;
            final JSONArray currentLogicList = getLogicByName(questionnaireList, getName(currentElement));
            if (currentLogicList != null && currentLogicList.length() > 0) {
                for (int j = 0; j < currentLogicList.length(); j += 1) {
                    logicList.put(currentLogicList.optJSONObject(j));
                }
            }
        }
        return logicList;
    }

    public static String getMatchingTargetElement(final JSONArray questionnaireList, final JSONObject currentQuestionItem) {
        // get all group elements from current questionnaire item
        final JSONArray elements = getElements(currentQuestionItem);
        // get list of all logic that match the name
        final JSONArray logicList = getMatchingLogic(questionnaireList, currentQuestionItem);
        // go through all logic and return the index of the match logic
        for (int i = 0; logicList != null && i < logicList.length(); i += 1) {
            if (logicList.optJSONObject(i) == null) continue;
            final JSONObject currentLogic = logicList.optJSONObject(i).optJSONObject("logic");
            final boolean hasAndLogic = hasLogic(currentLogic, true);
            final boolean hasOrLogic = hasLogic(currentLogic, false);
            if (!hasAndLogic && !hasOrLogic) {
                return currentLogic.optString("target");
            }
            if (matchedLogic(currentLogic.optJSONArray("and"), elements, true)) {
                return currentLogic.optString("target");
            }
            if (matchedLogic(currentLogic.optJSONArray("or"), elements, false)) {
                return currentLogic.optString("target");
            }
        }
        return null;
    }

    public static <T> void setResult(final JSONObject element, final T result) {
        try {
            element.put("result", result);
        } catch (Exception e) {
            // pass
        }
    }

    public static void setError(final JSONObject element, final boolean hasError) {
        try {
            element.put("hasError", hasError);
        } catch (Exception e) {
            // pass
        }
    }

    public static void clearElement(final JSONArray questionnaireList, final Stack<Integer> historyList, final int index) {
        if (questionnaireList == null) {
            return;
        }
        boolean shouldClear = false;
        for (int currentIndex : historyList) {
            if (currentIndex == index) {
                shouldClear = true;
            }
            if (shouldClear && currentIndex >= 0) {
                final JSONObject currentElement = questionnaireList.optJSONObject(currentIndex);
                final JSONArray elementList = getElements(currentElement);
                for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
                    if (elementList.optJSONObject(i) != null) {
                        elementList.optJSONObject(i).remove("result");
                        elementList.optJSONObject(i).remove("hasError");
                    }
                }
            }
        }
    }

    public static JSONArray getPreAudienceQuestionnaire(final JSONObject item) {
        return item.optJSONArray("preAudienceQuestionnaire");
    }

    public static JSONArray getPostAudienceQuestionnaire(final JSONObject item) {
        return item.optJSONArray("postAudienceQuestionnaire");
    }

}
