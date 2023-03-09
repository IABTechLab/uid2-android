package com.uid2.client

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import com.uid2.client.networking.DataEnvelope
import com.uid2.client.networking.PostUtil
import com.uid2.client.networking.refresh.RefreshAPIPackage
import com.uid2.client.networking.refresh.TokenRefreshResponse

class UID2Client(uid2BaseURL: String, context: Context) {
    private val refreshPath: String = "/v2/token/refresh"
    private var postUtil: PostUtil? = null

    init {
        try {
            val versionName =
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            postUtil = PostUtil(uid2BaseURL + refreshPath, listOf(Pair("X-UID2-Client-Version", "Android-$versionName")))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            throw UID2Exception("Failed to get package version")
        }
    }

    fun refreshIdentity(refreshToken: String, refreshResponseKey: String?): RefreshAPIPackage {
        return try {
            val response: String = postUtil!!.send(refreshToken)
            val decryptedResponseString = DataEnvelope.decrypt(
                response,
                Base64.decode(refreshResponseKey, Base64.DEFAULT)
            )
            val tokenRefreshResponse = TokenRefreshResponse(decryptedResponseString)
            tokenRefreshResponse.toRefreshAPIPackage()
        } catch (e: Exception) {
            throw UID2Exception("Error Happened during Refresh Token")
        }
    }
}