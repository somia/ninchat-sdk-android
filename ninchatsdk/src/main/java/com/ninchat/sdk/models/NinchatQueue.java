package com.ninchat.sdk.models;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 28/08/2018.
 */
public final class NinchatQueue {

    public NinchatQueue(String id, String name) {
        this.id = id;
        this.name = name;
    }

    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
