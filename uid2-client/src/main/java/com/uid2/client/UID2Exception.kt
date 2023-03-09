package com.uid2.client

open class UID2Exception(message: String) : Exception(message)

class UID2HTTPRequestException(message: String, val code: Int?): UID2Exception(message)

class UID2DecryptionException(message: String, val obj: String? ): UID2Exception(message)