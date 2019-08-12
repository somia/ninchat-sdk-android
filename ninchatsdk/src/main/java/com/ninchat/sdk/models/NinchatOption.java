package com.ninchat.sdk.models;

import org.json.JSONException;
import org.json.JSONObject;

public final class NinchatOption {

    private String label;
    private String value;
    private boolean selected = false;

    public NinchatOption(final JSONObject json) throws JSONException {
        this.label = json.getString("label");
        this.value = json.getString("value");
    }

    public void toggle() {
        this.selected = !this.selected;
    }

    public String getLabel() {
        return this.label;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject()
                .put("label", label)
                .put("value", value)
                .put("selected", selected);
    }
}
