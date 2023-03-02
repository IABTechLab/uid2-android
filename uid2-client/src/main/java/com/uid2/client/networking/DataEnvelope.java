package com.uid2.client.networking;

import android.util.Base64;

import java.nio.charset.Charset;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DataEnvelope {
    private static final int TIMESTAMP_LENGTH = 8;
    private static final int GCM_AUTHTAG_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;

    public static String decrypt(String response, byte[] secretKey, boolean isRefreshResponse, byte[] nonceInRequest) throws Exception {
        byte[] payload = decryptGCM(Base64.decode(response, Base64.DEFAULT), 0, secretKey);
        
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

    public static byte[] decryptGCM(byte[] encryptedBytes, int offset, byte[] secretBytes) {
        try {
            final SecretKeySpec key = new SecretKeySpec(secretBytes, "AES");
            final GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_AUTHTAG_LENGTH * 8, encryptedBytes, offset, GCM_IV_LENGTH);
            final Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
            return c.doFinal(encryptedBytes, offset + GCM_IV_LENGTH, encryptedBytes.length - offset - GCM_IV_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("Unable to Decrypt", e);
        }
    }
}
