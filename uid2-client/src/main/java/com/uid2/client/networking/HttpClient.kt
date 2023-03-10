package com.uid2.client.networking

import com.uid2.client.UID2HTTPRequestException
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpClient(private val url: String, private val headerList: List<Pair<String, String>>) {
    private var readTimeout: Int = 5000
    private var connectTimeout: Int = 5000

    fun post(data: String): String {
        return try {
            val conn: HttpURLConnection = URL(url).openConnection() as HttpURLConnection

            conn.readTimeout = readTimeout
            conn.connectTimeout = connectTimeout

            conn.doInput = true
            conn.doOutput = true

            conn.useCaches = false

            for (header in headerList) {
                conn.setRequestProperty(header.first, header.second)
            }

            conn.outputStream.write(data.toByteArray())
            conn.outputStream.flush()

            if (conn.responseCode == 200) {
                conn.responseMessage
                val message = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var len:Int = conn.inputStream.read(buffer)
                while (len != -1) {
                    message.write(buffer, 0, len)
                    len = conn.inputStream.read(buffer)
                }
                conn.inputStream.close()
                message.close()
                message.toString("UTF8")
            } else {
                throw UID2HTTPRequestException(conn.responseMessage, conn.responseCode)
            }
        } catch (e: Exception) {
            if (e is UID2HTTPRequestException) {
                throw e
            } else {
                throw UID2HTTPRequestException("Unknown Error", null)
            }
        }
    }
}
