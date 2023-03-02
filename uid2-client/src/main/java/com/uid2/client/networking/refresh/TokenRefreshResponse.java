package com.uid2.client.networking.refresh;

import com.uid2.client.data.IdentityStatus;
import com.uid2.client.data.UID2Identity;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenRefreshResponse {
    public String body;
    public String status;
    // public String message;

    public TokenRefreshResponse(String decryptedResponseString) throws JSONException {
        JSONObject responseJson = new JSONObject(decryptedResponseString);
        status = responseJson.getString("status");
        if (status.equals("success")) {
            body = responseJson.getString("body");
        }
    }

    public UID2Identity toUID2Identity() throws JSONException {
        return UID2Identity.fromJson(new JSONObject(body));
    }

    public RefreshAPIPackage toRefreshAPIPackage() throws JSONException {
        switch (status) {
            case "success": {
                return new RefreshAPIPackage(this.toUID2Identity(), IdentityStatus.REFRESHED, "Identity Refreshed");
            }
            case "optout":{
                return new RefreshAPIPackage(null, IdentityStatus.OPT_OUT, "User opted out");
            }
            case "invalid_token": {
                return new RefreshAPIPackage(null, IdentityStatus.REFRESH_EXPIRED, "Invalid Token");
            }
            default:{
                return null;
            }
        }
    }
}
