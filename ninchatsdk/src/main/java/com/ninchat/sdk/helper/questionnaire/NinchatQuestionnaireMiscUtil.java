package com.ninchat.sdk.helper.questionnaire;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.ninchat.sdk.NinchatSessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatQuestionnaireMiscUtil {
    public static final int DURATION = 500;

    public static boolean hasPattern(JSONObject element) {
        if (element == null) {
            return false;
        }
        // return element.has("pattern") && !TextUtils.isEmpty(element.optString("pattern", null));
        return element.has("pattern");
    }


    public static boolean matchPattern(String currentInput, String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            return true;
        }
        if ((currentInput == null ? "" : currentInput).equals(pattern == null ? "" : pattern)) {
            return true;
        }
        return (currentInput == null ? "" : currentInput).matches(pattern == null ? "" : pattern);
    }

    public static boolean matchPattern(JSONObject element) {
        if (element == null) {
            return false;
        }
        if (!hasPattern(element)) {
            return true;
        }
        if (isInput(element) || isTextArea(element) || isSelect(element) || isLikeRT(element) || isRadio(element)) {
            String value = getResultString(element);
            return matchPattern(value, getPattern(element));
        }
        return true;
    }


    public static boolean hasResult(JSONObject element) {
        if (isInput(element) || isTextArea(element) || isSelect(element) || isLikeRT(element) || isRadio(element)) {
            String value = getResultString(element);
            return !TextUtils.isEmpty(value);
        }
        if (isCheckBox(element)) {
            return getResultBoolean(element);
        }
        return true;
    }


    public static boolean isRequiredOK(JSONObject element) {
        boolean isRequired = isRequired(element);
        // if not an required field. then good
        if (!isRequired) {
            return true;
        }
        return hasResult(element);
    }

    public static boolean hasButton(JSONObject buttonElement, boolean isBack) {
        if (buttonElement == null) {
            return false;
        }
        if (TextUtils.isEmpty(buttonElement.optString(isBack ? "back" : "next"))) {
            return false;
        }
        return !"false".equalsIgnoreCase(buttonElement.optString(isBack ? "back" : "next"));
    }

    public static boolean hasButton(JSONObject element) {
        JSONObject buttons = element.optJSONObject("buttons");
        if (buttons == null) {
            return false;
        }
        boolean hasBack = hasButton(buttons, true);
        boolean hasNext = hasButton(buttons, false);
        return (hasBack || hasNext);
    }

    public static boolean isClosedQueue(String queueId) {
        return (NinchatSessionManager.getInstance().getQueue(queueId) == null || NinchatSessionManager.getInstance().getQueue(queueId).isClosed());
    }

    public static boolean isEqual(String a, String b) {
        return (a == null ? "" : a).equalsIgnoreCase(b == null ? "" : b);
    }

    public static JSONObject getSlowCopy(JSONObject element) {
        try {
            return new JSONObject(element.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static JSONArray getSlowCopy(JSONArray elementList) {
        JSONArray retval = new JSONArray();
        for (int i = 0; elementList != null && i < elementList.length(); i += 1) {
            retval.put(getSlowCopy(elementList.optJSONObject(i)));
        }
        return retval;
    }

    public static void setAnimation(final View itemView, final int position, final boolean notFirstItem) {
        itemView.setAlpha(0.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(itemView, "alpha", 0.f, 0.5f, 1.0f);
        ObjectAnimator.ofFloat(itemView, "alpha", 0.f).start();
        animator.setStartDelay(notFirstItem ? DURATION / 2 : (position * DURATION / 3));
        animator.setDuration(DURATION);
        animatorSet.play(animator);
        animator.start();
    }

    public static String sanitizeString(final String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        return text.replaceAll("^[\\n\\r]", "").replaceAll("[\n\r]$", "").trim();
    }
}
