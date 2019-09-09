package com.ninchat.sdk.models;

import org.json.JSONException;
import org.json.JSONObject;

public final class NinchatOption {

    private String label;
    private JSONObject data;
    private boolean selected = false;

    public NinchatOption(final JSONObject json) throws JSONException {
        this.label = json.getString("label");
        this.data = json;
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
        return this.data.put("selected", selected);
    }
}
