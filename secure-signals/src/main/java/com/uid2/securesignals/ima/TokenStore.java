package com.uid2.securesignals.ima;

public class TokenStore {
    private String tokens;

    public void setTokens(String tokens) {
        this.tokens = tokens;
    }

    public String getTokens() {
        return this.tokens;
    }

    public void clearTokens() {
        this.tokens = null;
    }
}
