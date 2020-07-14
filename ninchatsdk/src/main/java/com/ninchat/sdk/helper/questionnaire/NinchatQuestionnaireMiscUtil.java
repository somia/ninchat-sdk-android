package com.ninchat.sdk.helper.questionnaire;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatQuestionnaireMiscUtil {
    public static Spanned fromHTML(String source) {
        return source == null ? null : Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(source);
    }

    public static boolean hasPattern(final JSONObject element) {
        if (element == null) {
            return false;
        }
        return element.has("pattern");
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
}
