package com.ninchat.sdk.helper.questionnaire;


import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatQuestionnaireNavigationUtil {
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

}
