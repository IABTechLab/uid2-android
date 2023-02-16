package com.uid2.client;

import java.util.Timer;

public class RepeatingTimer {
    public int retryTimeInMilliseconds;
    private Timer timer = new Timer();

    public RepeatingTimer(int retryTimeInMilliseconds) {
        retryTimeInMilliseconds = retryTimeInMilliseconds;
    }
}
