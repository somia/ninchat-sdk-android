package com.ninchat.sdk.helper.questionnaire;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatQuestionnaireSantizer {
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

    public static JSONArray getThankYouElement(final String thankYouString, final boolean isRegister) throws JSONException {
        JSONObject thankYouElement = new JSONObject();
        thankYouElement.putOpt("element", "text");
        thankYouElement.putOpt("name", "ThankYouText");
        thankYouElement.putOpt("label", thankYouString);

        JSONObject buttonElement = new JSONObject("{\"element\":\"buttons\",\"fireEvent\":true,\"back\":false,\"next\":\"true\"}");
        buttonElement.putOpt("type", isRegister ? "_register" : "_complete");

        thankYouElement.putOpt("element", "text");
        thankYouElement.putOpt("name", "ThankYouText");
        thankYouElement.putOpt("label", thankYouString);


        JSONArray questionnaireList = new JSONArray();
        questionnaireList.put(thankYouElement);
        questionnaireList.put(buttonElement);


        final JSONArray retval = new JSONArray();
        final JSONObject simpleForm = new JSONObject();
        simpleForm.putOpt("name", "ThankYouForm");
        simpleForm.putOpt("type", "group");
        simpleForm.putOpt("buttons", buttonElement);
        simpleForm.putOpt("elements", questionnaireList);
        final JSONObject logic = new JSONObject("{\"name\":\"ThankYouForm-Logic1\",\"logic\":{\"target\":\"_register\"}}");
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
}