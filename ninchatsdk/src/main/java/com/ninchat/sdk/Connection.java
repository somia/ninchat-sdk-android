package com.ninchat.sdk;

import android.util.Log;

import com.ninchat.client.CloseHandler;
import com.ninchat.client.ConnStateHandler;
import com.ninchat.client.EventHandler;
import com.ninchat.client.LogHandler;
import com.ninchat.client.Payload;
import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.client.SessionEventHandler;
import com.ninchat.client.Strings;

public class Connection {

    /**
     * Test connection by creating a client session and trying to send a message.
     */
    public static void test() {
        Strings messageTypes = new Strings();
        messageTypes.append("ninchat.com/*");

        Props sessionParams = new Props();
        sessionParams.setStringArray("message_types", messageTypes);

        Session session = new Session();
        session.setOnSessionEvent(new SessionEventHandler() {
            @Override
            public void onSessionEvent(Props params) {
                Log.i("Example", "session_event: " + params.toString());
            }
        });
        session.setOnEvent(new EventHandler() {
            @Override
            public void onEvent(Props params, Payload payload, boolean lastReply) {
                Log.i("Example", "event: " + params.toString());
            }
        });
        session.setOnClose(new CloseHandler() {
            @Override
            public void onClose() {
                Log.i("Example", "closed");
            }
        });
        session.setOnConnState(new ConnStateHandler() {
            @Override
            public void onConnState(String state) {
                Log.d("Example", state);
            }
        });
        session.setOnLog(new LogHandler() {
            @Override
            public void onLog(String msg) {
                Log.d("Client", msg);
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
