package com.thetradedesk.securesignalsadapter;

import android.content.Context;

import androidx.annotation.Keep;

import com.google.ads.interactivemedia.v3.api.VersionInfo;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsAdapter;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsCollectSignalsCallback;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsInitializeCallback;
import com.uid2.client.IdentityTokens;

@Keep
public final class TTDSecureSignalsAdapter implements SecureSignalsAdapter {

    private static final VersionInfo AdapterVersion = new VersionInfo(1, 0, 1);
    private DataHelper dataHelper = SingletonFactory.getInstance();
    private IdentityTokens identityTokens;

    @Override
    public VersionInfo getSDKVersion() {
        return null;
    }

    @Override
    public VersionInfo getVersion() {
        return AdapterVersion;
    }

    @Override
    public void collectSignals(Context context, SecureSignalsCollectSignalsCallback secureSignalsCollectSignalsCallback) {
        try {
            // Collect and encrypt the signals.
            // TODO: Now `isDueForRefresh` returns true when timestamp is after refresh_from or advertising_token has expired
            //  we can consider separate these 2 cases to have better control
            if (identityTokens.isDueForRefresh()) {
                if (identityTokens.isRefreshable()) {
                    dataHelper.refreshToken(identityTokens);
                    // Just throw exception instead of waiting for the refresh to return because collect signals has a timeout of 500ms
                    throw new Exception("Identity Tokens are due to refresh while collecting signals, refreshing but signals not ready for collecting");
                } else {
                    dataHelper.sendMessage(TTDMessage.REFRESH_TOKEN_EXPIRED);
                    throw new Exception("Identity Tokens due to refresh but refresh token has expired");
                }
            }
            String signals = identityTokens.getJsonString();
            // Pass the encrypted signals to IMA SDK.
            secureSignalsCollectSignalsCallback.onSuccess(signals);
        } catch (Exception e) {
            // Pass signal collection failures to IMA SDK.
            secureSignalsCollectSignalsCallback.onFailure(e);
        }
    }

    @Override
    public void initialize(Context context, SecureSignalsInitializeCallback secureSignalsInitializeCallback) {
        try {
            // Initialize your SDK and any dependencies.
            // TODO: confirm with google side about the timing of initializing
            IdentityTokens it = dataHelper.getIdentityTokens();
            if (it.isRefreshable()) {
                dataHelper.refreshToken(it);
            } else {
                dataHelper.sendMessage(TTDMessage.REFRESH_TOKEN_EXPIRED);
                throw new Exception("Refresh token has expired");
            }
            identityTokens = dataHelper.getIdentityTokens();
            // Notify IMA SDK of initialization success.
            secureSignalsInitializeCallback.onSuccess();
        } catch (Exception e) {
            // Pass initialization failures to IMA SDK.
            secureSignalsInitializeCallback.onFailure(e);
        }
    }
}
