package com.uid2.client.networking.refresh;

import com.uid2.client.data.IdentityStatus;
import com.uid2.client.data.UID2Identity;

public class RefreshAPIPackage {
    public UID2Identity identity;
    public IdentityStatus status;
    public String message;

    public RefreshAPIPackage(UID2Identity _identity, IdentityStatus _status, String _message) {
        identity = _identity;
        status = _status;
        message = _message;
    }
}
