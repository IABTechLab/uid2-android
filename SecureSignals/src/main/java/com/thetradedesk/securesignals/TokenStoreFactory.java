package com.thetradedesk.securesignals;

public class TokenStoreFactory {
    private static TokenStore tokenStore = null;

    private static TokenStore InitTokenStore() {
        return new TokenStore();
    }

    public static TokenStore getInstance() {
        if (TokenStoreFactory.tokenStore != null) {
            TokenStoreFactory.tokenStore = TokenStoreFactory.InitTokenStore();
        }
        return TokenStoreFactory.tokenStore;
    }
}
