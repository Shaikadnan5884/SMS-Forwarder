package com.example.sms_forwroder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final String PREFS_NAME = "SmsForwarderPrefs";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String forwardTo = prefs.getString("forward_number", "");
            boolean forwardAll = prefs.getBoolean("forward_all", false);
            String senderId = prefs.getString("sender_id", "");
            
            boolean telegramEnabled = prefs.getBoolean("telegram_enabled", false);
            String botToken = prefs.getString("bot_token", "");
            String chatId = prefs.getString("chat_id", "");

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress();
                        String messageBody = smsMessage.getMessageBody();

                        Log.d(TAG, "SMS from: " + sender + ", Content: " + messageBody);

                        boolean shouldForward = forwardAll || (sender != null && !senderId.isEmpty() && sender.contains(senderId));

                        if (shouldForward) {
                            // Forward via SMS
                            if (!forwardTo.isEmpty() && prefs.getBoolean("sms_enabled", false)) {
                                forwardSms(context, forwardTo, messageBody, sender);
                            }
                            
                            // Forward via Telegram (WiFi)
                            if (telegramEnabled && !botToken.isEmpty() && !chatId.isEmpty()) {
                                forwardViaTelegram(botToken, chatId, messageBody, sender);
                            }
                        }
                    }
                }
            }
        }
    }

    private void forwardSms(Context context, String forwardTo, String message, String originalSender) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String forwardedText = "From [" + originalSender + "]: " + message;
            if (forwardedText.length() > 160) {
                smsManager.sendMultipartTextMessage(forwardTo, null, smsManager.divideMessage(forwardedText), null, null);
            } else {
                smsManager.sendTextMessage(forwardTo, null, forwardedText, null, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to forward SMS: " + e.getMessage());
        }
    }

    private void forwardViaTelegram(String token, String chatId, String message, String sender) {
        executor.execute(() -> {
            try {
                String text = "🔔 *New SMS Received*\n\n*From:* " + sender + "\n*Message:* " + message;
                URL url = new URL("https://api.telegram.org/bot" + token + "/sendMessage");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("chat_id", chatId);
                jsonParam.put("text", text);
                jsonParam.put("parse_mode", "Markdown");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Telegram Response Code: " + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Telegram Forward failed: " + e.getMessage());
            }
        });
    }
}
