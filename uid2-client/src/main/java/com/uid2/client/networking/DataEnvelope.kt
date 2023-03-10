package com.uid2.client.networking

import android.util.Base64
import com.uid2.client.UID2DecryptionException
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object DataEnvelope {
    private const val GCM_AUTH_TAG_LENGTH = 16
    private const val GCM_IV_LENGTH = 12

    fun decrypt(
        response: String,
        secretKey: ByteArray,
    ): String {
        return try {
            val encryptedBytes = Base64.decode(response, Base64.DEFAULT)
            val key = SecretKeySpec(secretKey, "AES")
            val gcmParameterSpec =
                GCMParameterSpec(GCM_AUTH_TAG_LENGTH * 8, encryptedBytes, 0, GCM_IV_LENGTH)
            val c = Cipher.getInstance("AES/GCM/NoPadding")
            c.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)
            val payload = c.doFinal(
                encryptedBytes,
                GCM_IV_LENGTH,
                encryptedBytes.size - GCM_IV_LENGTH
            )
            String(payload, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            throw UID2DecryptionException("Unable to Decrypt", response )
        }
    }
}