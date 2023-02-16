package com.uid2.client;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.uid2.client.data.IdentityPackage;
import com.uid2.client.data.IdentityStatus;
import com.uid2.client.data.UID2Identity;
import com.uid2.client.networking.refresh.RefreshAPIPackage;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.TimerTask;

public class UID2Manager {
    public static UID2Manager shared;

    private final String defaultUid2ApiUrl = "https://prod.uidapi.com";
    private final int defaultUid2RefreshRetry = 5000;
    private MutableLiveData<UID2Identity> identity;
    private MutableLiveData<IdentityStatus> identityStatus;
    private UID2Client client;
    private boolean autoRefreshEnabled = true;
    private RepeatingTimer timer;

    private UID2Manager(Context context) {
        // Initialize token persistence storage singleton
        StorageManager.getInstance(context);

        // Initialize uid2 request client
        String uid2ApiUrl = defaultUid2ApiUrl;
        client = new UID2Client(defaultUid2ApiUrl, context);

        // Initialize refresh timer
        int refreshTime = defaultUid2RefreshRetry;
        timer = new RepeatingTimer(refreshTime, new TimerTask() {
            @Override
            public void run() {
                try {
                    UID2Identity storedIdentity = StorageManager.shared.getIdentity();
                    if (storedIdentity == null) {
                        return;
                    }
                    UID2Identity validIdentity = validateAndSetIdentity(storedIdentity);
                    if (validIdentity == null) {
                        return;
                    }
                    triggerRefreshOrTimer(validIdentity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            UID2Identity storedIdentity = StorageManager.shared.getIdentity();
            if (storedIdentity != null) {
                identity.setValue(storedIdentity);
                identityStatus.setValue(IdentityStatus.ESTABLISHED);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static UID2Manager getInstance(Context context) {
        if (shared == null) {
            shared = new UID2Manager(context);
        }
        return shared;
    }

    public MutableLiveData<UID2Identity> getIdentity() {
        if (identity == null) {
            identity = new MutableLiveData<UID2Identity>(null);
        }
        return identity;
    }

    public MutableLiveData<IdentityStatus> getIdentityStatus() {
        if (identityStatus == null) {
            identityStatus = new MutableLiveData<IdentityStatus>(null);
        }
        return identityStatus;
    }

    public void setIdentity(UID2Identity _identity) {
        UID2Identity validIdentity = validateAndSetIdentity(_identity);
        if (validIdentity != null) {
            triggerRefreshOrTimer(validIdentity);
        }
    }

    public void setIdentity(JSONObject json) throws JSONException {
        setIdentity(UID2Identity.fromJson(json));
    }

    public void resetIdentity() {
        identity.setValue(null);
        identityStatus.setValue(null);
        StorageManager.shared.deleteIdentity();
    }

    /**
     * Manual Refresh
     */
    public void refreshIdentity() {
        UID2Identity currentIdentity = identity.getValue();
        if (currentIdentity != null) {
            refreshToken(currentIdentity);
        }
    }

    public String getAdvertisingToken() {
        return identity.getValue().advertisingToken;
    }

    public void toggleAutoTokenRefresh (boolean enabled) {
        if (autoRefreshEnabled == enabled) {
            return;
        }
        autoRefreshEnabled = enabled;
        if (enabled) {
            timer.resume();
        } else {
            timer.stop();
        }
    }

    private UID2Identity validateAndSetIdentity(UID2Identity uid2Identity) {
        return validateAndSetIdentity(uid2Identity, null, null);
    }

    private UID2Identity validateAndSetIdentity(UID2Identity uid2Identity, IdentityStatus status, String statusText) {
        if (status == IdentityStatus.OPT_OUT) {
            identity.setValue(null);
            StorageManager.shared.deleteIdentity();
            identityStatus.setValue(IdentityStatus.OPT_OUT);
            return null;
        }

        IdentityPackage validity = IdentityPackage.fromIdentity(uid2Identity);
        identityStatus.setValue(validity.status);
        UID2Identity validIdentity = validity.identity;
        if (validIdentity == null) {
            return null;
        }
        if (validIdentity.advertisingToken == identity.getValue().advertisingToken) {
            return validIdentity;
        }
        identity.setValue(validIdentity);
        StorageManager.shared.saveIdentity(validIdentity);
        return validIdentity;
    }

    private void refreshToken(UID2Identity identity) {
        RefreshAPIPackage response = client.refreshIdentity(identity.refreshToken, identity.refreshResponseKey);
        validateAndSetIdentity(response.identity, response.status,response.message);
    }

    private void triggerRefreshOrTimer(UID2Identity validIdentity) {
        if (validIdentity.refreshFrom.isAfter(Instant.now())) {
            refreshToken(validIdentity);
        } else if (autoRefreshEnabled) {
            timer.resume();
        }
    }
}
