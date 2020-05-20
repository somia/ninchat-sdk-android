package com.ninchat.sdk.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class NinchatMessageList {
    private List<String> ids;
    private Map<String, NinchatMessage> idMap;

    public NinchatMessageList() {
        ids = new ArrayList<>();
        idMap = new HashMap<>();
    }

    public int add(final String writingId, final NinchatMessage ninchatMessage) {
        ids.add(writingId);
        idMap.put(writingId, ninchatMessage);
        Collections.sort(ids);
        return ids.indexOf(writingId);
    }

    public void remove(final String writingPrefix, final String writingId) {
        StringBuilder sb = new StringBuilder();
        if (writingPrefix != null) {
            sb.append(writingPrefix);
        }
        if (writingId != null) {
            sb.append(writingId);
        }
        ids.remove(sb.toString());
        Collections.sort(ids);
    }

    public boolean contains(final String writingId) {
        return ids.contains(writingId);
    }

    public int getPosition(final String writingId) {
        return ids.indexOf(writingId);
    }

    public long getItemId(final int position) {
        return ids.get(position).hashCode();
    }

    public NinchatMessage getMessage(final int position) {
        final String writingId = ids.get(position);
        return idMap.get(writingId);
    }

    public String getLastMessageId(final boolean allowMeta) {
        if (isEmpty()) {
            return ""; // Imaginary message id preceding all actual ids.
        }
        final ListIterator<String> iterator = ids.listIterator(size() - 1);
        while (iterator.hasPrevious() && !allowMeta) {
            final String id = iterator.previous();
            final NinchatMessage message = idMap.get(id);
            if (message != null &&
                    (message.getType() == NinchatMessage.Type.MESSAGE ||
                            message.getType() == NinchatMessage.Type.MULTICHOICE)) {
                return id;
            }
        }
        return ids.get(size() - 1);
    }

    public int size() {
        return ids.size();
    }

    public boolean isEmpty() {
        return ids.isEmpty();
    }

    public void clear() {
        ids.clear();
    }
}
