package com.example.sms_forwroder;

public class SmsMessageModel {
    private String body;
    private String sender;
    private long timestamp;

    public SmsMessageModel(String body, String sender, long timestamp) {
        this.body = body;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getBody() {
        return body;
    }

    public String getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
