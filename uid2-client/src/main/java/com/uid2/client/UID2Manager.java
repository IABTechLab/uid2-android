package com.uid2.client;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import com.uid2.client.data.IdentityPackage;
import com.uid2.client.data.IdentityStatus;
import com.uid2.client.data.UID2Identity;
import com.uid2.client.networking.refresh.RefreshAPIPackage;
import com.uid2.client.UID2Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;

public class UID2Manager {
    public static UID2Manager shared;

    private final String defaultUid2ApiUrl = "https://prod.uidapi.com";
    private final int defaultUid2RefreshRetry = 5000;
    private MutableLiveData<UID2Identity> identity;
    private MutableLiveData<IdentityStatus> identityStatus;
    private UID2Client client;
    private boolean autoRefreshEnabled;
    private RepeatingTimer timer;

    private UID2Manager(Context context) {
        // Initialize token persistence storage singleton
        StorageManager.getInstance(context);

        // get saved auto refresh preference
        autoRefreshEnabled = StorageManager.shared.getAutoRefreshPreference();

        // Get configuration from app settings
        String uid2ApiUrl = defaultUid2ApiUrl;
        int refreshTime = defaultUid2RefreshRetry;
        try {
            Bundle bundle = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
            uid2ApiUrl = (String)bundle.get("uid2_api_url");
            refreshTime = (int)bundle.get("uid2_refresh_retry_time");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Initialize uid2 request client
        client = new UID2Client(uid2ApiUrl, context);

        // Initialize uid2 live data
        identity = new MutableLiveData<UID2Identity>(null);
        identityStatus = new MutableLiveData<IdentityStatus>(IdentityStatus.NO_IDENTITY);

        // Initialize refresh timer
        timer = new RepeatingTimer(refreshTime, autoRefreshEnabled);

        // If token persisted, take it out.
        try {
            UID2Identity storedIdentity = StorageManager.shared.getIdentity();
            if (storedIdentity != null) {
                validateAndSetIdentity(storedIdentity, IdentityStatus.ESTABLISHED, null);
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
        UID2Identity validIdentity = validateAndSetIdentity(_identity, IdentityStatus.ESTABLISHED, null);
        if (validIdentity != null) {
            triggerRefreshOrTimer(validIdentity);
        }
    }

    public void setIdentity(JSONObject json) throws JSONException {
        setIdentity(UID2Identity.fromJson(json));
    }

    public void resetIdentity() {
        identity.postValue(null);
        identityStatus.postValue(IdentityStatus.NO_IDENTITY);
        StorageManager.shared.deleteIdentity();
    }

    /**
     * Manual Refresh
     */
    public void refreshIdentity() {
        IdentityStatus status = identityStatus.getValue();
        if (status.canBeRefreshed()) {
            refreshToken(identity.getValue());
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
        StorageManager.shared.saveAutoRefreshPreference(enabled);
        if (enabled) {
            timer.resume();
        } else {
            timer.stop();
        }
    }

    public boolean getAutoRefreshed() {
        return autoRefreshEnabled;
    }

    private UID2Identity validateAndSetIdentity(UID2Identity uid2Identity) {
        return validateAndSetIdentity(uid2Identity, null, null);
    }

    private UID2Identity validateAndSetIdentity(UID2Identity uid2Identity, IdentityStatus status, String statusText) {
        if (status == IdentityStatus.OPT_OUT) {
            identity.postValue(null);
            StorageManager.shared.deleteIdentity();
            identityStatus.postValue(IdentityStatus.OPT_OUT);
            return null;
        }

        IdentityPackage validity = IdentityPackage.fromIdentity(uid2Identity, status == IdentityStatus.ESTABLISHED);
        identityStatus.postValue(validity.status);
        UID2Identity validIdentity = validity.identity;
        if (validIdentity == null) {
            return null;
        }
        if (identity.getValue() != null && validIdentity.advertisingToken == identity.getValue().advertisingToken) {
            return validIdentity;
        }
        identity.postValue(validIdentity);
        StorageManager.shared.saveIdentity(validIdentity);
        return validIdentity;
    }

    private void refreshToken(UID2Identity identity) {
        try {
            RefreshAPIPackage response = client.refreshIdentity(identity.refreshToken, identity.refreshResponseKey);
            validateAndSetIdentity(response.identity, response.status,response.message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void triggerRefreshOrTimer(UID2Identity validIdentity) {
        if (validIdentity.refreshFrom.isBefore(Instant.now())) {
            refreshToken(validIdentity);
        } else if (autoRefreshEnabled) {
            timer.resume();
        }
    }
}
