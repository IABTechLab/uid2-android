package com.thetradedesk.securesignalsadapter;

import android.os.Handler;
import android.os.Message;
import android.util.LruCache;

import com.uid2.client.IdentityTokens;
import com.uid2.client.PublisherUid2Client;
import com.uid2.client.TokenRefreshResponse;

import java.util.OptionalInt;
import java.util.Timer;
import java.util.TimerTask;

public class DataHelper {
    private final LruCache<String, IdentityTokens> cache = new LruCache<String, IdentityTokens>(100);
    private static final String ADVERTISING_TOKEN = "ADVERTISING_TOKEN";
    private static final long REFRESH_INTERVAL = 5 * 60 * 1000;
    private final Timer timer = new Timer();
    private PublisherUid2Client client;
    private Handler handler;

    public DataHelper setUid2ClientParams(String uid2BaseUrl, String clientApiKey, String base64SecretKey, Handler handler) {
        client = new PublisherUid2Client(uid2BaseUrl, clientApiKey, base64SecretKey);
        handler = handler;
        return this;
    }



    public DataHelper setIdentityTokens(IdentityTokens it) {
        this.cache.put(DataHelper.ADVERTISING_TOKEN, it);
        // Start a timer task to refresh token after token is set.
        refreshTokenTimer(it);
        return this;
    }

    public IdentityTokens getIdentityTokens() {
        return this.cache.get(DataHelper.ADVERTISING_TOKEN);
    }

    /**
     * TODO: Do we need a timer task to refresh token? If yes, what is the time interval?
     * @param it identity tokens to refresh
     */
    private void refreshTokenTimer(IdentityTokens it) {
        timer.cancel();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    refreshToken(it, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, REFRESH_INTERVAL, REFRESH_INTERVAL);
    }

    public void refreshToken(IdentityTokens it, Integer retry) throws Exception {
        if (it.isRefreshable()) {
            TokenRefreshResponse res = client.refreshToken(it);
            if (res.isSuccess()) {
                setIdentityTokens(res.getIdentity());
            } else if (res.isOptout()) {
                clearIdentityTokens();
                sendMessage(TTDMessage.OPTOUT);
            } else if (retry < 3) {
                refreshToken(it, retry + 1);
            } else {
                sendMessage(TTDMessage.REFRESH_TOKEN_RETRY_FAILED);
                throw new Exception("Failed after retry refreshing for 3 times.");
            }
        } else {
            sendMessage(TTDMessage.REFRESH_TOKEN_EXPIRED);
            clearIdentityTokens();
            throw new Exception("identity stored cannot be refreshed, please generate new identity");
        }
    }

    /**
     * send message to app
     * @param message
     */
    public void sendMessage(TTDMessage message) {
        if (handler != null) {
            Message message1 = handler.obtainMessage(1, message);
            handler.sendMessage(message1);
        }
    }

    /**
     * Used when:
     * 1. User opted out
     * 2. Refresh Token has expired
     * 3. Android developer actively clear it.
     */
    public void clearIdentityTokens() {
        this.cache.remove(ADVERTISING_TOKEN);
    }
}
