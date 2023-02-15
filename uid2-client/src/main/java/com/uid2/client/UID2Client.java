package com.uid2.client;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UID2Client {
    private String uid2BaseURL;
    private final static MediaType FORM = MediaType.get("application/x-www-form-urlencoded");
    private final OkHttpClient client = new OkHttpClient();
    private Headers headers;

    public UID2Client(String uid2BaseURL, Context context) {
        uid2BaseURL = uid2BaseURL;
        try {
            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            headers = new Headers.Builder().add("X-UID2-Client-Version: Android-" + versionCode).build();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void refreshIdentity(String refreshToken, String refreshResponseKey) {
        Request request = new Request.Builder()
                .url(uid2BaseURL)
                .headers(headers)
                .post(RequestBody.create(refreshToken, FORM))
                .build();
        try (Response response = client.newCall(request).execute()) {

        } catch (IOException e) {

        }
    }
}
