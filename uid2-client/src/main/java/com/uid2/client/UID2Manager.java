package com.uid2.client;

import android.content.Context;

import com.uid2.client.data.UID2Identity;

public class UID2Manager {
    public static UID2Manager shared;
    public UID2Identity uid2Identity;

    private UID2Manager(Context context) {
        StorageManager.getInstance(context);
    }

    public static UID2Manager getInstance(Context context) {
        if (shared == null) {
            shared = new UID2Manager(context);
        }
        return shared;
    }
}
