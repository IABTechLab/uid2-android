package com.uid2.client.data;

public enum IdentityStatus {
    ESTABLISHED,
    REFRESHED,
    EXPIRED,
    NO_IDENTITY,
    INVALID,
    REFRESH_EXPIRED,
    OPT_OUT;
    public boolean canBeRefreshed() {
        return this == ESTABLISHED || this == REFRESHED || this == EXPIRED;
    }
}


