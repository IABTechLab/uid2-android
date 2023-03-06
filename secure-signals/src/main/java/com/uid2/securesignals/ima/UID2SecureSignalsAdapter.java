package com.uid2.securesignals.ima;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;

import com.google.ads.interactivemedia.v3.api.VersionInfo;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsAdapter;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsCollectSignalsCallback;
import com.google.ads.interactivemedia.v3.api.signals.SecureSignalsInitializeCallback;
import com.uid2.client.UID2Manager;
import com.uid2.securesignals.BuildConfig;

@Keep
public final class UID2SecureSignalsAdapter implements SecureSignalsAdapter {

    private String versionName;

    @Override
    public VersionInfo getSDKVersion() {
        return getVersionInfo(BuildConfig.IMA_LIB_VERSION);
    }

    @Override
    public VersionInfo getVersion() {
        return getVersionInfo(versionName);
    }

    @Override
    public void collectSignals(Context context, SecureSignalsCollectSignalsCallback secureSignalsCollectSignalsCallback) {
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
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

    private VersionInfo getVersionInfo(String versionName) {
        String splits[] = versionName.split("\\.");
        if (splits.length >= 3) {
            int major = Integer.parseInt(splits[0]);
            int minor = Integer.parseInt(splits[1]);
            int micro = Integer.parseInt(splits[2]);
            return new VersionInfo(major, minor, micro);
        }

        String logMessage = String.format("Unexpected version format: %s." +
                "Returning 0.0.0 for version.", versionName);
        Log.w("UID2MediationAdapter", logMessage);
        return new VersionInfo(0, 0, 0);
    }
}
