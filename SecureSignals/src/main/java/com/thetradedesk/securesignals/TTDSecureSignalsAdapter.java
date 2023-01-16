package com.thetradedesk.securesignals;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Keep;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.ads.interactivemedia.v3.api.VersionInfo;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsAdapter;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsCollectSignalsCallback;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsInitializeCallback;

@Keep
public final class TTDSecureSignalsAdapter implements SecureSignalsAdapter {

    private static final VersionInfo AdapterVersion = new VersionInfo(0, 0, 1);
    private static final VersionInfo SDKVersion = new VersionInfo(3, 29,0);
    private TokenStore tokenStore = TokenStoreFactory.getInstance();
    private LocalBroadcastManager localBroadCastManager;

    @Override
    public VersionInfo getSDKVersion() {
        return SDKVersion;
    }

    @Override
    public VersionInfo getVersion() {
        return AdapterVersion;
    }

    @Override
    public void collectSignals(Context context, SecureSignalsCollectSignalsCallback secureSignalsCollectSignalsCallback) {
        try {
            // Collect and encrypt the signals.
            secureSignalsCollectSignalsCallback.onSuccess(tokenStore.getTokens());
        } catch (Exception e) {
            // Pass signal collection failures to IMA SDK.
            Intent intent = new Intent("com.thetradedesk.securesignals");
            intent.putExtra("exception", e);
            this.localBroadCastManager.sendBroadcast(intent);
            secureSignalsCollectSignalsCallback.onFailure(e);
        }
    }

    @Override
    public void initialize(Context context, SecureSignalsInitializeCallback secureSignalsInitializeCallback) {
        try {
            // Initialize your SDK and any dependencies.
            this.localBroadCastManager = LocalBroadcastManager.getInstance(context);
            // Notify IMA SDK of initialization success.
            secureSignalsInitializeCallback.onSuccess();
        } catch (Exception e) {
            // Pass initialization failures to IMA SDK.
            Intent intent = new Intent("com.thetradedesk.securesignals");
            intent.putExtra("exception", e);
            this.localBroadCastManager.sendBroadcast(intent);
            secureSignalsInitializeCallback.onFailure(e);
        }
    }
}
