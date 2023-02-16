package com.uid2.client;

import android.content.Context;
import android.content.SharedPreferences;

import com.uid2.client.data.UID2Identity;

import org.json.JSONException;

public class StorageManager {
    private static String PREFERENCE_FILE_KEY = "com.uid2.client.storage";
    private static String IDENTITY_PACKAGE_KEY = "com.uid2.client.storage.identity";
    private SharedPreferences sp;
    public static StorageManager shared;

    public static StorageManager getInstance(Context context) {
        if (shared == null) {
            shared = new StorageManager(context);
        }
        return shared;
    }

    private StorageManager(Context context) {
        sp = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
    }

    public void saveIdentity(UID2Identity identity) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(IDENTITY_PACKAGE_KEY, identity.getJsonString());
        editor.apply();
    }

    public UID2Identity getIdentity() throws JSONException {
        String identityJsonString = sp.getString(IDENTITY_PACKAGE_KEY, null);
        if (identityJsonString == null) {
            return null;
        }
        return UID2Identity.fromJsonString(identityJsonString);
    }

    public void deleteIdentity() {
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(IDENTITY_PACKAGE_KEY);
        editor.apply();
    }
}
