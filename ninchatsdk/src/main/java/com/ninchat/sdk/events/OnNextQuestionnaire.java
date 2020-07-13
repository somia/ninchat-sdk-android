package com.ninchat.sdk.events;

public class OnNextQuestionnaire {
    public int moveType;

    public OnNextQuestionnaire(final int moveType) {
        this.moveType = moveType;
    }

    public static final int back = 0;
    public static final int forward = 1;
    public static final int other = 2;
}
