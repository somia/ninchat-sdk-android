package com.ninchat.sdk.events;

public class OnCompleteQuestionnaire {
    public boolean openQueueView;
    public String queueId;
    public OnCompleteQuestionnaire(final boolean openQueueView, final String queueId) {
        this.openQueueView = openQueueView;
        this.queueId = queueId;
    }
}
