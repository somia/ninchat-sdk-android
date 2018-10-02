package com.ninchat.sdk.models;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 28/08/2018.
 */
public final class NinchatQueue {

    public NinchatQueue(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    private String id;
    private String name;
    private long position = Long.MAX_VALUE;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }
}
