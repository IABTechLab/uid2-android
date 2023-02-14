package com.uid2.client.data;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.time.Instant;

public class UID2Identity {
    public String advertisingToken;
    public String refreshToken;
    public Instant identityExpires;
    public Instant refreshFrom;
    public Instant refreshExpires;
    public String refreshResponseKey;
    public String jsonString;

    private UID2Identity(String advertisingToken, String refreshToken, String refreshResponseKey, Instant refreshFrom, Instant refreshExpires, Instant identityExpires, String jsonString) {
        this.advertisingToken = advertisingToken;
        this.refreshToken = refreshToken;
        this.identityExpires = identityExpires;
        this.refreshExpires = refreshExpires;
        this.refreshFrom = refreshFrom;
        this.refreshResponseKey = refreshResponseKey;
        this.jsonString = jsonString;
    }

    public static UID2Identity fromJsonString(String jsonString) throws JSONException {
        return fromJson(new JSONObject(jsonString), jsonString);
    }

    static public UID2Identity fromJson(JSONObject json, String jsonString) throws JSONException {
        return new UID2Identity(getJsonString(json, "advertising_token"), getJsonString(json, "refresh_token"), getRefreshResponseKey(json),
                getInstant(json, "identity_expires"), getInstant(json, "refresh_expires"), getInstant(json, "refresh_from"), jsonString);
    }

    static public UID2Identity fromJson(JSONObject json) throws JSONException {
        return fromJson(json, json.toString());
    }

    static private String getJsonString(JSONObject json, String key) throws JSONException {
        return json.getString(key);
    }

    static private String getRefreshResponseKey(JSONObject json) {
        try {
            return getJsonString(json, "refresh_response_key");
        } catch (JSONException e) {
            return null;
        }
    }

    static private Instant getInstant(JSONObject json, String key) throws JSONException {
        return Instant.parse(getJsonString(json, key));
    }

    public String getJsonString() {
        return jsonString;
    }
}
