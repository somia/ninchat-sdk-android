package com.ninchat.sdk.helper.questionnaire;


import android.text.TextUtils;

import com.ninchat.client.Props;
import com.ninchat.client.Strings;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaires;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireLogicUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatQuestionnaireItemGetter {
    public static JSONArray getElements(final JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optJSONArray("elements");
    }


    public static String getPattern(final JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optString("pattern", null);
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


    public static Strings getTags(final JSONArray tagList) {
        Strings tags = new Strings();
        for (int i = 0; tagList != null && i < tagList.length(); i += 1) {
            if (tagList.optString(i) != null)
                tags.append(tagList.optString(i));
        }
        return tags;
    }

    public static NinchatQuestionnaire getQuestionnaire(final int questionnaireType) {
        final NinchatQuestionnaires questionnaires = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires();

        return questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE ?
                questionnaires.getNinchatPreAudienceQuestionnaire() : questionnaires.getNinchatPostAudienceQuestionnaire();
    }

    public static String getAudienceRegisteredText(final int questionnaireType) {
        final NinchatQuestionnaires questionnaires = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires();

        return questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE ?
                questionnaires.getAudienceRegisteredText() : "";
    }

    public static String getAudienceRegisteredClosedText(final int questionnaireType) {
        final NinchatQuestionnaires questionnaires = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires();

        return questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE ?
                questionnaires.getAudienceRegisteredClosedText() : "";
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

    public static String getAudienceRegisteredTextFromConfig(final JSONObject item) {
        return item.optString("audienceRegisteredText", "");
    }

    public static String getAudienceRegisteredClosedTextFromConfig(final JSONObject item) {
        return item.optString("audienceRegisteredClosedText", "");
    }

    public static JSONArray getPreAudienceQuestionnaire(@NotNull final JSONObject item) {
        return item.optJSONArray("preAudienceQuestionnaire");
    }

    public static JSONArray getPostAudienceQuestionnaire(@NotNull final JSONObject item) {
        return item.optJSONArray("postAudienceQuestionnaire");
    }

    public static boolean isConversationLikePreAudienceQuestionnaire(final JSONObject item) {
        return item.optString("preAudienceQuestionnaireStyle", "").equalsIgnoreCase("conversation");
    }

    public static boolean isConversationLikePostAudienceQuestionnaire(final JSONObject item) {
        return item.optString("postAudienceQuestionnaireStyle", "").equalsIgnoreCase("conversation");
    }


}
