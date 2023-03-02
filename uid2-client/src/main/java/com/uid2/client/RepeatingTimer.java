package com.uid2.client;

import java.util.Timer;
import java.util.TimerTask;

public class RepeatingTimer {
    private int retryTimeInMilliseconds;
    private Timer timer;

    private enum State {
        SUSPENDED,
        RESUMED
    }
    private State state = State.SUSPENDED;

    public RepeatingTimer(int _retryTimeInMilliseconds, boolean autoRefreshEnabled) {
        retryTimeInMilliseconds = _retryTimeInMilliseconds;
        if (autoRefreshEnabled) {
            resume();
        }
    }

    public void resume() {
        if (state == State.RESUMED) {
            return;
        }
        if (timer == null) {
            timer = new Timer();
        }
        state = State.RESUMED;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                UID2Manager.shared.refreshIdentity();
            }
        }, retryTimeInMilliseconds, retryTimeInMilliseconds);
    }

    public void stop() {
        if (state == State.SUSPENDED) {
            return;
        }
        state = State.SUSPENDED;
        timer.cancel();
        timer.purge();
        timer = null;
    }
}
