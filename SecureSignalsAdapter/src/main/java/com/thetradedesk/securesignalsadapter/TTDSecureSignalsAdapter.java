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
            //  (e.g. decide to whether to pass the advertising token when it needs refreshing according to whether it's expired)
            IdentityTokens it = dataHelper.getIdentityTokens();
            if (it != null) {
                if (it.isDueForRefresh()) {
                    try {
                        dataHelper.refreshToken(it, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Pass the encrypted signals to IMA SDK.
                secureSignalsCollectSignalsCallback.onSuccess(it.getJsonString());
            } else {
                // If stored identity tokens are null, user may have opted out, or refresh token has expired.
                secureSignalsCollectSignalsCallback.onSuccess("");
            }
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
            if (it.isDueForRefresh()) {
                try {
                    dataHelper.refreshToken(it, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Notify IMA SDK of initialization success.
            secureSignalsInitializeCallback.onSuccess();
        } catch (Exception e) {
            // Pass initialization failures to IMA SDK.
            secureSignalsInitializeCallback.onFailure(e);
        }
    }
}
