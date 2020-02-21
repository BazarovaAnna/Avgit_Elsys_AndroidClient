package com.example.elsysandroid;

public interface PollTaskListener {
    void onError(String message);
    void onMessage(String message);
}
