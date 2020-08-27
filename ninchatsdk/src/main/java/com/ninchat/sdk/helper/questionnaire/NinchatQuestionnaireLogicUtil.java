package com.ninchat.sdk.helper.questionnaire;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.getResultString;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireNavigationUtil.getQuestionnaireElementByName;

public class NinchatQuestionnaireLogicUtil {

    public static boolean hasLogic(JSONObject logicElement, boolean isAnd) {
        if (logicElement == null) {
            return false;
        }
        JSONArray logicList = logicElement.optJSONArray(isAnd ? "and" : "or");
        for (int i = 0; logicList != null && i < logicList.length(); i += 1) {
            JSONObject currentLogic = logicList.optJSONObject(i);
            if (currentLogic == null) continue;
            Iterator<String> keys = currentLogic.keys();
            while (keys.hasNext()) {
                String currentKey = keys.next();
                // if current key is not empty
                if (!TextUtils.isEmpty(currentKey)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean matchedLogicCore(String key, String value, JSONArray elementList) {
        JSONObject matchedElement = getQuestionnaireElementByName(elementList, key);
        if (matchedElement == null) {
            return false;
        }
        String result = getResultString(matchedElement);
        return matchPattern(result, value);
    }

    public static boolean matchedLogic(JSONArray logicList, JSONArray elementList, boolean matchAnd) {
        if (logicList == null) {
            return false;
        }
        boolean ok = matchAnd ? true : false;
        for (int i = 0; i < logicList.length(); i += 1) {
            JSONObject currentLogic = logicList.optJSONObject(i);
            Iterator<String> keys = currentLogic.keys();
            while (keys.hasNext()) {
                String currentKey = keys.next();
                // if current key is empty, then just ignore and continue
                if (TextUtils.isEmpty(currentKey)) {
                    continue;
                }
                String currentValue = currentLogic.optString(currentKey);
                boolean matched = matchedLogicCore(currentKey, currentValue, elementList);
                ok = matchAnd ? ok & matched : ok | matched;
            }
        }
        return ok;
    }
}
