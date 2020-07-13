package com.ninchat.sdk.helper;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;


import com.ninchat.client.JSON;
import com.ninchat.client.Props;
import com.ninchat.client.Strings;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaires;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    public static final int PRE_AUDIENCE_QUESTIONNAIRE = 1;
    public static final int POST_AUDIENCE_QUESTIONNAIRE = 2;
    public static final int DEFAULT_INT_VALUE = -1;

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
            if (currentElement != null && name.equals(currentElement.optString("name"))) {
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

    public static boolean hasPattern(final JSONObject element) {
        if (element == null) {
            return false;
        }
        return element.has("pattern");
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
        if (!hasPattern(element)) {
            return true;
        }
        if (isInput(element) || isTextArea(element) || isSelect(element) || isLikeRT(element) || isRadio(element)) {
            final String value = getResultString(element);
            return matchPattern(value, getPattern(element));
        }
        return true;
    }


    public static boolean hasResult(final JSONObject element) {
        if (isInput(element) || isTextArea(element) || isSelect(element) || isLikeRT(element) || isRadio(element)) {
            final String value = getResultString(element);
            return !TextUtils.isEmpty(value);
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
        return !"false".equalsIgnoreCase(buttonElement.optString(isBack ? "back" : "next"));
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
                JSONObject tempElement = getButtonElement(currentElement, i == 0);
                // hardcoded the last element of the next button needs to be always true
                if (!hasButton(tempElement, false)) {
                    tempElement.putOpt("next", true);
                }
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

    public static JSONArray updateLikeRTElements(final JSONArray questionnaireList) throws JSONException {
        for (int i = 0; questionnaireList != null && i < questionnaireList.length(); i += 1) {
            final JSONObject currentElement = questionnaireList.optJSONObject(i);
            if (currentElement == null) continue;
            if (isLogic(currentElement)) continue;
            JSONArray elementList = getElements(currentElement);
            for (int j = 0; elementList != null && j < elementList.length(); j += 1) {
                final JSONObject likeRTElement = elementList.optJSONObject(j);
                if (!isLikeRT(likeRTElement)) continue;
                likeRTElement.putOpt("options", getLikeRTOptions());
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
        // make like rt elements with options
        return updateLikeRTElements(updateActions(retval));
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

    public static JSONObject getMatchingLogic(final JSONArray questionnaireList, JSONArray allFilledGroupElementList, final JSONObject currentQuestionItem) {
        // get list of all logic that match the name
        final JSONArray logicList = getMatchingLogic(questionnaireList, currentQuestionItem);
        // go through all logic and return the index of the match logic
        for (int i = 0; logicList != null && i < logicList.length(); i += 1) {
            if (logicList.optJSONObject(i) == null) continue;
            final JSONObject currentLogic = logicList.optJSONObject(i).optJSONObject("logic");
            final boolean hasAndLogic = hasLogic(currentLogic, true);
            final boolean hasOrLogic = hasLogic(currentLogic, false);
            if (!hasAndLogic && !hasOrLogic) {
                return currentLogic;
            }
            if (matchedLogic(currentLogic.optJSONArray("and"), allFilledGroupElementList, true)) {
                return currentLogic;
            }
            if (matchedLogic(currentLogic.optJSONArray("or"), allFilledGroupElementList, false)) {
                return currentLogic;
            }
        }
        return null;
    }

    public static String getMatchingLogicTarget(final JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optString("target");
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

    public static void setTags(final JSONObject logic, final JSONObject currentElement) {
        if (logic == null || currentElement == null) return;
        final JSONArray tags = logic.optJSONArray("tags");
        if (tags != null && tags.length() > 0) {
            try {
                currentElement.putOpt("tags", tags);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setQueue(final JSONObject logic, JSONObject currentElement) {
        if (logic == null || currentElement == null) return;
        final String queue = logic.optString("queue");
        if (TextUtils.isEmpty(queue)) return;
        try {
            currentElement.putOpt("queue", queue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONArray getAllFilledElements(final JSONArray questionnaireList, final Stack<Integer> historyList) {
        JSONArray retval = new JSONArray();
        if (questionnaireList == null) {
            return retval;
        }
        for (int currentIndex : historyList) {
            final JSONObject currentElement = questionnaireList.optJSONObject(currentIndex);
            final JSONArray elementList = getElements(currentElement);
            for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
                if (elementList.optJSONObject(i) != null) {
                    retval.put(elementList.optJSONObject(i));
                }
            }
        }
        return retval;
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
                        elementList.optJSONObject(i).remove("tags");
                        elementList.optJSONObject(i).remove("queue");
                    }
                }
            }
        }
    }

    public static int getOptionIndex(final JSONObject element, final String result) {
        if (TextUtils.isEmpty(result)) return -1;

        final JSONArray optionList = getOptions(element);
        for (int i = 0; optionList != null && i < optionList.length(); i += 1) {
            final JSONObject currentOption = optionList.optJSONObject(i);
            final String value = getValue(currentOption);
            if (result.equalsIgnoreCase(value)) return i;
        }
        return -1;
    }

    public static String getOptionValueByIndex(final JSONObject element, final int index) {
        final JSONArray optionList = getOptions(element);
        if (optionList == null || index < 0) {
            return null;
        }
        final JSONObject currentItem = index >= optionList.length() ? null : optionList.optJSONObject(index);
        return getValue(currentItem);
    }

    public static JSONArray getLikeRTOptions() throws JSONException {
        JSONArray options = new JSONArray("[{\"label\":\"Strongly disagree\",\"value\":\"strongly_disagree\"},{\"label\":\"Disagree\",\"value\":\"disagree\"},{\"label\":\"Neither agree nor disagree\",\"value\":\"neither_agree_nor_disagree\"},{\"label\":\"Agree\",\"value\":\"agree\"},{\"label\":\"Strongly agree\",\"value\":\"strongly_agree\"}]");
        return options;
    }

    public static int updateRequiredFieldStats(final JSONObject element) {
        int index = -1;
        final JSONArray elementList = getElements(element);
        for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
            final JSONObject currentElement = elementList.optJSONObject(i);
            final boolean requiredOk = isRequiredOK(currentElement);
            final boolean patternOk = matchPattern(currentElement);
            setError(currentElement, !(requiredOk && patternOk));
            // take only the first item. for focusing purpose only
            if ((!requiredOk || !patternOk) && index == -1) {
                index = i;
            }
        }
        return index;
    }

    public static boolean formHasError(final JSONObject element) {
        int index = -1;
        final JSONArray elementList = getElements(element);
        for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
            final JSONObject currentElement = elementList.optJSONObject(i);
            final boolean requiredOk = isRequiredOK(currentElement);
            final boolean patternOk = matchPattern(currentElement);
            // take only the first item. for focusing purpose only
            if ((!requiredOk || !patternOk) && index == -1) {
                return true;
            }
        }
        return false;
    }

    public static void clearElementResult(final JSONObject element) {
        final JSONArray elementList = getElements(element);
        if (elementList == null || elementList.length() == 0) {
            return;
        }
        JSONObject lastElement = elementList.optJSONObject(elementList.length() - 1);
        if (lastElement != null) {
            lastElement.remove("result");
            lastElement.remove("hasError");
            lastElement.remove("tags");
            lastElement.remove("queue");
        }
    }

    public static JSONObject getQuestionnaireAnswers(final JSONArray questionnaireList, final Stack<Integer> historyList) {
        JSONObject retval = new JSONObject();
        List<Integer> seen = new ArrayList<>();
        for (int currentIndex : historyList) {
            if (currentIndex < 0) continue;
            if (seen.contains(currentIndex)) continue;
            seen.add(currentIndex);
            final JSONObject currentElement = questionnaireList.optJSONObject(currentIndex);
            final JSONArray elementList = getElements(currentElement);
            for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
                if (elementList.optJSONObject(i) == null) continue;
                if (isLogic(elementList.optJSONObject(i)) || isButton(elementList.optJSONObject(i)) || isText(elementList.optJSONObject(i)))
                    continue;
                final String elementName = getName(elementList.optJSONObject(i));
                final String result = getResultString(elementList.optJSONObject(i));
                if (TextUtils.isEmpty(result)) continue;
                try {
                    retval.putOpt(elementName, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return retval;
    }

    public static JSONArray getQuestionnaireAnswersTags(final JSONArray questionnaireList, final Stack<Integer> historyList) {
        JSONArray retval = new JSONArray();
        List<Integer> seen = new ArrayList<>();
        for (int currentIndex : historyList) {
            if (currentIndex < 0) continue;
            if (seen.contains(currentIndex)) continue;
            seen.add(currentIndex);
            final JSONObject currentElement = questionnaireList.optJSONObject(currentIndex);
            final JSONArray tagList = currentElement == null ? null : currentElement.optJSONArray("tags");
            for (int i = 0; tagList != null && i < tagList.length(); i += 1) {
                if (tagList.optString(i) == null) continue;
                retval.put(tagList.optString(i));
            }
        }
        return retval;
    }

    public static String getQuestionnaireAnswersQueue(final JSONArray questionnaireList, final Stack<Integer> historyList) {
        for (int currentIndex : historyList) {
            if (currentIndex < 0) continue;
            final JSONObject currentElement = questionnaireList.optJSONObject(currentIndex);
            final String queue = currentElement == null ? null : currentElement.optString("queue");
            return queue;
        }
        return null;
    }

    public static Props getPreAnswers(final String resultString) {
        Props preAnswers = new Props();
        try {
            final JSONObject result = new JSONObject(resultString);
            Iterator<String> keys = result.keys();
            while (keys.hasNext()) {
                final String currentKey = keys.next();
                // if current key is empty
                if (TextUtils.isEmpty(currentKey)) continue;
                if (currentKey.equalsIgnoreCase("tags")) {
                    final Strings tags = getTags(result.optJSONArray("tags"));
                    if (tags.length() > 0) preAnswers.setStringArray("tags", tags);
                } else {
                    preAnswers.setString(currentKey, result.optString(currentKey, ""));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return preAnswers;
    }

    public static Props getPreAnswers(final JSONObject result) {
        Props preAnswers = new Props();
        Iterator<String> keys = result.keys();
        while (keys.hasNext()) {
            final String currentKey = keys.next();
            // if current key is empty
            if (TextUtils.isEmpty(currentKey)) continue;
            if (currentKey.equalsIgnoreCase("tags")) {
                final Strings tags = getTags(result.optJSONArray("tags"));
                if (tags.length() > 0) preAnswers.setStringArray("tags", tags);
            } else {
                preAnswers.setString(currentKey, result.optString(currentKey, ""));
            }
        }
        return preAnswers;
    }

    public static Strings getTags(final JSONArray tagList) {
        Strings tags = new Strings();
        for (int i = 0; tagList != null && i < tagList.length(); i += 1) {
            if (tagList.optString(i) != null)
                tags.append(tagList.optString(i));
        }
        return tags;
    }

    public static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof LinearLayout) {
            if (view.getId() == -1) {
                view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.ninchat_chat_bubble_left_repeated_disabled));
            }
        }
        if (view instanceof Button) {
            view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.ninchat_chat_disable_button));
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public static JSONObject getFinalAnswers(final JSONObject answerList, final JSONArray tagList) {
        if (answerList == null || answerList.length() == 0) {
            return null;
        }
        try {
            answerList.putOpt("tags", tagList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return answerList;
    }

    public static com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire getQuestionnaire(final int questionnaireType) {
        final NinchatQuestionnaires questionnaires = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires();
        return questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE ?
                questionnaires.getNinchatPreAudienceQuestionnaire() : questionnaires.getNinchatPostAudienceQuestionnaire();
    }

    public static JSONArray getPreAudienceQuestionnaire(final JSONObject item) {
        return item.optJSONArray("preAudienceQuestionnaire");
    }

    public static JSONArray getPostAudienceQuestionnaire(final JSONObject item) {
        return item.optJSONArray("postAudienceQuestionnaire");
    }

}
