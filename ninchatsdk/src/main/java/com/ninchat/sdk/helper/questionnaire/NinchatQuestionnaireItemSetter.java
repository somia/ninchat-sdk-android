package com.ninchat.sdk.helper.questionnaire;


import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ninchat.sdk.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Stack;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.getElements;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;

public class NinchatQuestionnaireItemSetter {
    public static <T> void setResult(JSONObject element, T result) {
        try {
            element.put("result", result);
        } catch (Exception e) {
            // pass
        }
    }

    public static void setError(JSONObject element, boolean hasError) {
        try {
            element.put("hasError", hasError);
        } catch (Exception e) {
            // pass
        }
    }

    public static void setPosition(JSONObject element, int position) {
        try {
            element.put("position", position);
        } catch (Exception e) {
            // pass
        }
    }

    public static void setTags(JSONObject logic, JSONObject currentElement) {
        if (logic == null || currentElement == null) return;
        JSONArray tags = logic.optJSONArray("tags");
        if (tags != null && tags.length() > 0) {
            try {
                currentElement.putOpt("tags", tags);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setQueue(JSONObject logic, JSONObject currentElement) {
        if (logic == null || currentElement == null) return;
        String queue = logic.optString("queue");
        if (TextUtils.isEmpty(queue)) return;
        try {
            currentElement.putOpt("queue", queue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void clearElement(JSONArray questionnaireList, Stack<Integer> historyList, int index) {
        if (questionnaireList == null) {
            return;
        }
        boolean shouldClear = false;
        for (int currentIndex : historyList) {
            if (currentIndex == index) {
                shouldClear = true;
            }
            if (shouldClear && currentIndex >= 0) {
                JSONObject currentElement = questionnaireList.optJSONObject(currentIndex);
                JSONArray elementList = getElements(currentElement);
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


    public static void updateRequiredFieldStats(JSONObject element) {
        JSONArray elementList = getElements(element);
        for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
            JSONObject currentElement = elementList.optJSONObject(i);
            boolean requiredOk = isRequiredOK(currentElement);
            boolean patternOk = matchPattern(currentElement);
            setError(currentElement, !(requiredOk && patternOk));
        }
    }

    public static boolean formHasError(JSONObject element) {
        JSONArray elementList = getElements(element);
        for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
            JSONObject currentElement = elementList.optJSONObject(i);
            boolean requiredOk = isRequiredOK(currentElement);
            boolean patternOk = matchPattern(currentElement);
            // take only the first item. for focusing purpose only
            if ((!requiredOk || !patternOk)) {
                return true;
            }
        }
        return false;
    }

    public static int getFirstErrorIndex(JSONObject element) {
        JSONArray elementList = getElements(element);
        for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
            JSONObject currentElement = elementList.optJSONObject(i);
            boolean requiredOk = isRequiredOK(currentElement);
            boolean patternOk = matchPattern(currentElement);
            // take only the first item. for focusing purpose only
            if ((!requiredOk || !patternOk)) {
                return i;
            }
        }
        return 0;
    }

    public static void clearElementResult(JSONObject element) {
        if (element == null) return;
        element.remove("result");
        element.remove("position");
        element.remove("hasError");
        element.remove("tags");
        element.remove("queue");
        JSONArray elementList = getElements(element);
        for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
            JSONObject currentElement = elementList.optJSONObject(i);
            if (currentElement == null) continue;
            if (currentElement.optBoolean("hasError", false)) {
                currentElement.remove("position");
                currentElement.remove("tags");
                currentElement.remove("queue");
            }
        }
    }


    public static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof LinearLayout) {
            if (view.getId() == -1) {
                view.setBackground(ContextCompat.getDrawable(view.getContext(),
                        enabled ? R.drawable.ninchat_chat_bubble_left_repeated : R.drawable.ninchat_chat_bubble_left_repeated_disabled));
            }
        }
        /*if (view instanceof Button) {
            view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.ninchat_chat_disable_button));
        }*/
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public static JSONObject mergeAnswersAndTags(JSONObject answerList, JSONArray tagList) {
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
}
