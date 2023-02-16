package com.uid2.client;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.uid2.client.data.IdentityStatus;
import com.uid2.client.data.UID2Identity;

public class UID2Manager {
    public static UID2Manager shared;

    private final String defaultUid2ApiUrl = "https://prod.uidapi.com";
    private MutableLiveData<UID2Identity> identity;
    private MutableLiveData<IdentityStatus> identityStatus;
    private UID2Client client;

    private UID2Manager(Context context) {
        // Initialize token persistence storage singleton
        StorageManager.getInstance(context);

        // Initialize uid2 request client
        String uid2ApiUrl = defaultUid2ApiUrl;
        client = new UID2Client(defaultUid2ApiUrl, context);

        // Initialize refresh timer
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

    public static UID2Manager getInstance(Context context) {
        if (shared == null) {
            shared = new UID2Manager(context);
        }
        return shared;
    }
}
