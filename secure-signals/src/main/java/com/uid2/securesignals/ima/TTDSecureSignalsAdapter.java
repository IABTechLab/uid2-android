package com.uid2.securesignals.ima;

import android.content.Context;

import androidx.annotation.Keep;

import com.google.ads.interactivemedia.v3.api.VersionInfo;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsAdapter;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsCollectSignalsCallback;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsInitializeCallback;
import com.uid2.client.UID2Manager;

@Keep
public final class TTDSecureSignalsAdapter implements SecureSignalsAdapter {

    private static final VersionInfo AdapterVersion = new VersionInfo(0, 0, 1);
    private static final VersionInfo SDKVersion = new VersionInfo(3, 29,0);

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
            secureSignalsCollectSignalsCallback.onSuccess(UID2Manager.shared.getIdentity().getValue().getJsonString());
        } catch (Exception e) {
            // Pass signal collection failures to IMA SDK.
            secureSignalsCollectSignalsCallback.onFailure(e);
        }
    }

    @Override
    public void initialize(Context context, SecureSignalsInitializeCallback secureSignalsInitializeCallback) {
        try {
            // Initialize your SDK and any dependencies.
            // Notify IMA SDK of initialization success.
            secureSignalsInitializeCallback.onSuccess();
        } catch (Exception e) {
            // Pass initialization failures to IMA SDK.
            secureSignalsInitializeCallback.onFailure(e);
        }
    }
}
