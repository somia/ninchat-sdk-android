package com.ninchat.sdk.helper;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import com.ninchat.sdk.adapters.NinchatPreAudienceQuestionnaireAdapter;

import org.json.JSONObject;

public class NinchatQuestionnaire {
    public static final int UNKNOWN = 0;
    public static final int TEXT = 1;
    public static final int INPUT = 2;
    public static final int TEXT_AREA = 3;
    public static final int SELECT = 4;
    public static final int RADIO = 5;
    public static final int LIKERT = 6;
    public static final int CHECKBOX = 7;

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
        }
        return NinchatQuestionnaire.UNKNOWN;
    }

    public static Spanned fromHTML(String source) {
        return source == null ? null : Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY) : Html.fromHtml(source);
    }

}
