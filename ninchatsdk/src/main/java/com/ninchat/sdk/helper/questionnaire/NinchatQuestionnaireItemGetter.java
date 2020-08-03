package com.ninchat.sdk.helper.questionnaire;


import android.text.TextUtils;

import com.ninchat.client.Props;
import com.ninchat.client.Strings;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireHolder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireLogicUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatQuestionnaireItemGetter {
    public static JSONArray getElements(JSONObject jsonObject) {
        return jsonObject == null ? null : jsonObject.optJSONArray("elements");
    }

    public static String getPattern(JSONObject jsonObject) {
        return jsonObject == null ? null : jsonObject.optString("pattern", null);
    }

    public static String getLabel(JSONObject jsonObject) {
        boolean isRequired = isRequired(jsonObject);
        return jsonObject == null ? null : jsonObject.optString("label", null) + (isRequired ? " *" : "");
    }

    public static String getName(JSONObject element) {
        if (element == null) {
            return "";
        }
        return element.optString("name", null);
    }

    public static String getValue(JSONObject element) {
        if (element == null) {
            return "";
        }
        return element.optString("value", null);
    }


    public static JSONArray getOptions(JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optJSONArray("options");
    }


    public static String getResultString(JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optString("result", null);
    }

    public static int getOptionPosition(JSONObject element) {
        if (element == null) {
            return -1;
        }
        return element.optInt("position", -1);
    }

    public static boolean getResultBoolean(JSONObject element) {
        if (element == null) {
            return false;
        }
        return element.optBoolean("result", false);
    }

    public static boolean getError(JSONObject element) {
        if (element == null) {
            return false;
        }
        return element.optBoolean("hasError", false);
    }


    public static JSONObject getButtonElement(JSONObject element, boolean hideBack) {
        JSONObject retval = new JSONObject();
        try {
            JSONObject buttons = element.optJSONObject("buttons");
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
            retval.putOpt("next", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }

    public static JSONArray getLogicByName(JSONArray questionnaireList, String name) {
        JSONArray retval = new JSONArray();
        if (TextUtils.isEmpty(name)) {
            return retval;
        }
        for (int i = 0; questionnaireList != null && i < questionnaireList.length(); i += 1) {
            JSONObject currentElement = questionnaireList.optJSONObject(i);
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

    public static JSONArray getMatchingLogic(JSONArray questionnaireList, JSONObject groupQuestionnaire) {
        JSONArray logicList = getLogicByName(questionnaireList, getName(groupQuestionnaire));
        JSONArray elementList = getElements(groupQuestionnaire);
        for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
            JSONObject currentElement = elementList.optJSONObject(i);
            if (currentElement == null) continue;
            JSONArray currentLogicList = getLogicByName(questionnaireList, getName(currentElement));
            if (currentLogicList != null && currentLogicList.length() > 0) {
                for (int j = 0; j < currentLogicList.length(); j += 1) {
                    logicList.put(currentLogicList.optJSONObject(j));
                }
            }
        }
        return logicList;
    }

    public static JSONObject getMatchingLogic(JSONArray questionnaireList,
                                              JSONArray completedQuestionnaireList,
                                              JSONObject currentQuestionItem) {
        // get list of all logic that match the name
        JSONArray logicList = getMatchingLogic(questionnaireList, currentQuestionItem);
        JSONArray allElements = getAllFilledElements(completedQuestionnaireList);
        // go through all logic and return the index of the match logic
        for (int i = 0; logicList != null && i < logicList.length(); i += 1) {
            if (logicList.optJSONObject(i) == null) continue;
            JSONObject currentLogic = logicList.optJSONObject(i).optJSONObject("logic");
            boolean hasAndLogic = hasLogic(currentLogic, true);
            boolean hasOrLogic = hasLogic(currentLogic, false);
            if (!hasAndLogic && !hasOrLogic) {
                return currentLogic;
            }
            if (matchedLogic(currentLogic.optJSONArray("and"), allElements, true)) {
                return currentLogic;
            }
            if (matchedLogic(currentLogic.optJSONArray("or"), allElements, false)) {
                return currentLogic;
            }
        }
        return null;
    }

    public static String getMatchingLogicTarget(JSONObject element) {
        if (element == null) {
            return null;
        }
        return element.optString("target");
    }

    public static JSONArray getAllFilledElements(JSONArray questionnaireList) {
        JSONArray retval = new JSONArray();
        if (questionnaireList == null) {
            return retval;
        }
        List<String> seen = new ArrayList<>();
        for (int at = questionnaireList.length() - 1; at >= 0; at -= 1) {
            JSONObject currentElement = questionnaireList.optJSONObject(at);
            if (seen.contains(getName(currentElement))) continue;
            seen.add(getName(currentElement));
            JSONArray elementList = getElements(currentElement);
            for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
                if (elementList.optJSONObject(i) != null) {
                    retval.put(elementList.optJSONObject(i));
                }
            }
        }
        return retval;
    }

    public static int getOptionIndex(JSONObject element, String result) {
        if (TextUtils.isEmpty(result)) return -1;

        JSONArray optionList = getOptions(element);
        for (int i = 0; optionList != null && i < optionList.length(); i += 1) {
            JSONObject currentOption = optionList.optJSONObject(i);
            String value = getValue(currentOption);
            if (result.equalsIgnoreCase(value)) return i;
        }
        return -1;
    }

    public static String getOptionValueByIndex(JSONObject element, int index) {
        JSONArray optionList = getOptions(element);
        if (optionList == null || index < 0) {
            return null;
        }
        JSONObject currentItem = index >= optionList.length() ? null : optionList.optJSONObject(index);
        return getValue(currentItem);
    }

    public static JSONArray getLikeRTOptions() throws JSONException {
        JSONArray options = new JSONArray("[{\"label\":\"Strongly disagree\",\"value\":\"strongly_disagree\"},{\"label\":\"Disagree\",\"value\":\"disagree\"},{\"label\":\"Neither agree nor disagree\",\"value\":\"neither_agree_nor_disagree\"},{\"label\":\"Agree\",\"value\":\"agree\"},{\"label\":\"Strongly agree\",\"value\":\"strongly_agree\"}]");
        return options;
    }


    public static Strings getTags(JSONArray tagList) {
        Strings tags = new Strings();
        for (int i = 0; tagList != null && i < tagList.length(); i += 1) {
            if (tagList.optString(i) != null)
                tags.append(tagList.optString(i));
        }
        return tags;
    }

    public static String getAudienceRegisteredText(int questionnaireType) {
        NinchatQuestionnaireHolder questionnaires = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaireHolder();

        return questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE ?
                questionnaires.getAudienceRegisteredText() : "";
    }

    public static String getAudienceRegisteredClosedText(int questionnaireType) {
        NinchatQuestionnaireHolder questionnaires = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaireHolder();

        return questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE ?
                questionnaires.getAudienceRegisteredClosedText() : "";
    }

    public static Props getPreAnswers(JSONObject result) {
        Props preAnswers = new Props();
        if (result == null)
            return preAnswers;

        Iterator<String> keys = result.keys();
        while (keys.hasNext()) {
            String currentKey = keys.next();
            // if current key is empty
            if (TextUtils.isEmpty(currentKey)) continue;
            if (currentKey.equalsIgnoreCase("tags")) {
                Strings tags = getTags(result.optJSONArray("tags"));
                if (tags.length() > 0) preAnswers.setStringArray("tags", tags);
            } else {
                preAnswers.setString(currentKey, result.optString(currentKey, ""));
            }
        }
        return preAnswers;
    }

    public static JSONObject getQuestionnaireAnswers(JSONArray questionnaireList) {
        JSONObject retval = new JSONObject();
        List<String> seen = new ArrayList<>();
        if (questionnaireList == null) return retval;
        for (int i = questionnaireList.length() - 1; i >= 0; i -= 1) {
            JSONObject currentElement = questionnaireList.optJSONObject(i);
            if (seen.contains(getName(currentElement))) continue;
            seen.add(getName(currentElement));
            JSONArray elementList = getElements(currentElement);
            for (int j = 0; elementList != null && j < elementList.length(); j += 1) {
                if (elementList.optJSONObject(j) == null) continue;
                if (isLogic(elementList.optJSONObject(j)) || isButton(elementList.optJSONObject(j)) || isText(elementList.optJSONObject(j)))
                    continue;
                String elementName = getName(elementList.optJSONObject(j));
                String result = getResultString(elementList.optJSONObject(j));
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

    public static JSONArray getQuestionnaireAnswersTags(JSONArray questionnaireList) {
        JSONArray retval = new JSONArray();
        List<String> seen = new ArrayList<>();
        if (questionnaireList == null) return retval;
        for (int i = questionnaireList.length() - 1; i >= 0; i -= 1) {
            JSONObject currentElement = questionnaireList.optJSONObject(i);
            if (seen.contains(getName(currentElement))) continue;
            seen.add(getName(currentElement));
            JSONArray tagList = currentElement == null ? null : currentElement.optJSONArray("tags");
            for (int j = 0; tagList != null && j < tagList.length(); j += 1) {
                if (tagList.optString(j) == null) continue;
                retval.put(tagList.optString(j));
            }
        }
        return retval;
    }

    public static String getQuestionnaireAnswersQueue(JSONArray questionnaireList) {
        if (questionnaireList == null) {
            return null;
        }
        for (int i = questionnaireList.length() - 1; i >= 0; i -= 1) {
            JSONObject currentElement = questionnaireList.optJSONObject(i);
            String queue = currentElement == null ? null : currentElement.optString("queue", null);
            if (!TextUtils.isEmpty(queue)) {
                return queue;
            }
        }
        return null;
    }

    public static String getAudienceRegisteredTextFromConfig(JSONObject item) {
        return item.optString("audienceRegisteredText", "");
    }

    public static String getAudienceRegisteredClosedTextFromConfig(JSONObject item) {
        return item.optString("audienceRegisteredClosedText", "");
    }

    public static JSONArray getPreAudienceQuestionnaire(@NotNull JSONObject item) {
        return item.optJSONArray("preAudienceQuestionnaire");
    }

    public static JSONArray getPostAudienceQuestionnaire(@NotNull JSONObject item) {
        return item.optJSONArray("postAudienceQuestionnaire");
    }

    public static boolean isConversationLikePreAudienceQuestionnaire(JSONObject item) {
        return item.optString("preAudienceQuestionnaireStyle", "").equalsIgnoreCase("conversation");
    }

    public static boolean isConversationLikePostAudienceQuestionnaire(JSONObject item) {
        return item.optString("postAudienceQuestionnaireStyle", "").equalsIgnoreCase("conversation");
    }
}
