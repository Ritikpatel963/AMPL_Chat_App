package com.agromarket.ampl_chat.utils;

import android.content.Context;
import android.util.Log;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.util.HttpAuthorizer;

import java.util.HashMap;
import java.util.Map;

public class RealtimeManager {

    private Pusher pusher;
    private Channel channel;

    public void connect(Context context, int myId, int otherId, String token, MessageListener listener) {

        HttpAuthorizer authorizer = new HttpAuthorizer(
                "https://amplchat.agromarket.co.in/broadcasting/auth"
        );

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        authorizer.setHeaders(headers);

        PusherOptions options = new PusherOptions()
                .setAuthorizer(authorizer)
                .setEncrypted(true)
                .setHost("amplchat.agromarket.co.in")
                .setWsPort(6001)
                .setWssPort(443);

        pusher = new Pusher("amplchat-key", options);
        pusher.connect();

        String channelName = "private-chat." +
                Math.min(myId, otherId) + "." +
                Math.max(myId, otherId);

        PrivateChannel channel = pusher.subscribePrivate(channelName);

        channel.bind("message.sent", new PrivateChannelEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                Log.d("PUSHER", "Message received: " + event.getData());
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                Log.d("PUSHER", "Subscribed to " + channelName);
            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                Log.e("PUSHER", "Auth failed: " + message, e);
            }
        });
    }

    public void disconnect() {
        if (pusher != null) {
            pusher.disconnect();
        }
    }

    public interface MessageListener {
        void onNewMessage(String data);
    }
}