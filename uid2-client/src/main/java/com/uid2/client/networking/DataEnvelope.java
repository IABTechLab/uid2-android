package com.uid2.client.networking;

import android.util.Base64;

import java.nio.charset.Charset;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DataEnvelope {
    private static final int TIMESTAMP_LENGTH = 8;

    public static String decrypt(String response, byte[] secretKey, boolean isRefreshResponse, byte[] nonceInRequest) throws Exception {
        byte[] responseBytes = Base64.decode(response, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey, "AES"));
        byte[] payload = cipher.doFinal(responseBytes);

        byte[] resultBytes;
        if (!isRefreshResponse) {
            byte[] nonceInResponse = Arrays.copyOfRange(payload, TIMESTAMP_LENGTH, TIMESTAMP_LENGTH + nonceInRequest.length);
            if (!Arrays.equals(nonceInResponse, nonceInRequest)) {
                throw new Exception("Nonce in request does not match nonce in response");
            }
            resultBytes = Arrays.copyOfRange(payload, TIMESTAMP_LENGTH + nonceInRequest.length, payload.length);
        }
        else {
            resultBytes = payload;
        }
        return new String(resultBytes, Charset.forName("UTF-8"));
    }
}
