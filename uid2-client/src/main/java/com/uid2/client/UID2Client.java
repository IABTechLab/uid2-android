package com.uid2.client;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Base64;

import com.uid2.client.networking.DataEnvelope;
import com.uid2.client.networking.refresh.RefreshAPIPackage;
import com.uid2.client.networking.refresh.TokenRefreshResponse;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UID2Client {
    private final String refreshPath = "/v2/token/refresh";
    private final String uid2BaseURL;
    private final static MediaType FORM = MediaType.get("application/x-www-form-urlencoded");
    private final OkHttpClient client = new OkHttpClient();
    private Headers headers;

    public UID2Client(String _uid2BaseURL, Context context) {
        uid2BaseURL = _uid2BaseURL + refreshPath;
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            headers = new Headers.Builder().add("X-UID2-Client-Version: Android-" + versionName).build();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public RefreshAPIPackage refreshIdentity(String refreshToken, String refreshResponseKey) throws Exception {
        Request request = new Request.Builder()
                .url(uid2BaseURL)
                .headers(headers)
                .post(RequestBody.create(refreshToken, FORM))
                .build();
        try (Response response = client.newCall(request).execute()) {
            final String responseString = response.body().string();
            if (!response.isSuccessful()) {
                throw new Exception("Unexpected code " + response + " " + responseString);
            }

            String decryptedResponseString = DataEnvelope.decrypt(responseString, Base64.decode(refreshResponseKey, Base64.DEFAULT), true, null);
            TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse(decryptedResponseString);
            return tokenRefreshResponse.toRefreshAPIPackage();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}
