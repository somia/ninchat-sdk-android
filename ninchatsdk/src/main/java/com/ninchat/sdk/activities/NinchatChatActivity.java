package com.ninchat.sdk.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.ChatMessageRecyclerViewAdapter;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatChatActivity extends BaseActivity {

    static int REQUEST_CODE = NinchatChatActivity.class.hashCode() & 0xffff;

    private ChatMessageRecyclerViewAdapter messageAdapter;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_chat;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final RecyclerView messages = findViewById(R.id.message_list);
        messageAdapter = new ChatMessageRecyclerViewAdapter();
        messages.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        messages.setAdapter(messageAdapter);
    }
}
