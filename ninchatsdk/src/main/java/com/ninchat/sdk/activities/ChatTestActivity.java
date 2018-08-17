package com.ninchat.sdk.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.ninchat.client.CloseHandler;
import com.ninchat.client.ConnStateHandler;
import com.ninchat.client.EventHandler;
import com.ninchat.client.LogHandler;
import com.ninchat.client.Payload;
import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.client.SessionEventHandler;
import com.ninchat.client.Strings;
import com.ninchat.sdk.R;


public class ChatTestActivity extends BaseActivity {

    private TextView log;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_chat_test;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log = findViewById(R.id.log);
        test();
    }

    private void test() {
        Strings messageTypes = new Strings();
        messageTypes.append("ninchat.com/*");

        Props sessionParams = new Props();
        sessionParams.setStringArray("message_types", messageTypes);

        Session session = new Session();
        session.setOnSessionEvent(new SessionEventHandler() {
            @Override
            public void onSessionEvent(Props params) {
                log.append("\nsession_event: " + params.toString());
            }
        });
        session.setOnEvent(new EventHandler() {
            @Override
            public void onEvent(Props params, Payload payload, boolean lastReply) {
                log.append("\nevent: " + params.toString());
            }
        });
        session.setOnClose(new CloseHandler() {
            @Override
            public void onClose() {
                log.append("\nsession closed");
            }
        });
        session.setOnConnState(new ConnStateHandler() {
            @Override
            public void onConnState(String state) {
                log.append("\nconnection state: " + state);
            }
        });
        session.setOnLog(new LogHandler() {
            @Override
            public void onLog(String msg) {
                log.append("\nclient: " + msg);
            }
        });

        try {
            session.setParams(sessionParams);
            session.open();
            Props sendParams = new Props();
            sendParams.setString("action", "send_message");
            sendParams.setString("user_id", "007");
            sendParams.setString("message_type", "ninchat.com/no-such-message-type");

            Payload sendPayload = new Payload();
            String payload = "{\"text\":\"asdf\"}";
            sendPayload.append(payload.getBytes());

            session.send(sendParams, sendPayload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
