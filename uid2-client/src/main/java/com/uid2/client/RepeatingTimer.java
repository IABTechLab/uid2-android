package com.uid2.client;

import java.util.Timer;
import java.util.TimerTask;

public class RepeatingTimer {
    private int retryTimeInMilliseconds;
    private Timer timer;
    private TimerTask task;

    private enum State {
        SUSPENDED,
        RESUMED
    }
    private State state = State.SUSPENDED;

    public RepeatingTimer(int retryTimeInMilliseconds, TimerTask timerTask) {
        retryTimeInMilliseconds = retryTimeInMilliseconds;
        task = timerTask;
        timer = new Timer();
    }

    public void resume() {
        if (state == State.RESUMED) {
            return;
        }
        if (timer == null) {
            timer = new Timer();
        }
        state = State.RESUMED;
        timer.schedule(task, retryTimeInMilliseconds, retryTimeInMilliseconds);
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
