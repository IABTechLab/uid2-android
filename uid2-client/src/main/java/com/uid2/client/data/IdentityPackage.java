package com.uid2.client.data;

import java.time.Instant;

public class IdentityPackage {
    public boolean valid;
    public String message;
    public UID2Identity identity;
    public IdentityStatus status;

    public IdentityPackage(boolean _valid, String _message, UID2Identity _identity, IdentityStatus _status) {
        valid = _valid;
        message = _message;
        identity = _identity;
        status = _status;
    }

    public static IdentityPackage fromIdentity(UID2Identity identity) {
        return IdentityPackage.fromIdentity(identity, false);
    }

    public static IdentityPackage fromIdentity(UID2Identity identity, boolean isEstablish) {
        if (identity == null) {
            return new IdentityPackage(false, "Identity Not Available", null, IdentityStatus.NO_IDENTITY);
        }

        if (isEmpty(identity.advertisingToken)) {
            return new IdentityPackage(false, "advertising_token is not available or is not valid", null, IdentityStatus.INVALID);
        }

        if (isEmpty(identity.refreshToken)) {
            return new IdentityPackage(false, "refresh_token is not available or is not valid", null, IdentityStatus.INVALID);
        }

        if (hasExpired(identity.refreshExpires)) {
            return new IdentityPackage(false, "Identity expired, refresh expired", null, IdentityStatus.REFRESH_EXPIRED);
        }

        if (hasExpired(identity.identityExpires)) {
            return new IdentityPackage(true, "Identity expired, refresh still valid", identity, IdentityStatus.EXPIRED);
        }

        if (isEstablish) {
            return new IdentityPackage(true, "Identity established", identity, IdentityStatus.ESTABLISHED);
        }

        return new IdentityPackage(true, "Identity refreshed", identity, IdentityStatus.REFRESHED);
    }

    private static boolean isEmpty (String str) {
        return str == null || str == "";
    }

    private static boolean hasExpired(Instant expiry) {
        return Instant.now().isAfter(expiry);
    }
}
