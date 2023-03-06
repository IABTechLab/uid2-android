package com.uid2.securesignals.gma;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.VersionInfo;
import com.google.android.gms.ads.mediation.rtb.RtbAdapter;
import com.google.android.gms.ads.mediation.rtb.RtbSignalData;
import com.google.android.gms.ads.mediation.rtb.SignalCallbacks;
import com.uid2.client.UID2Manager;
import com.uid2.gms.BuildConfig;

import java.util.List;

@Keep
public class UID2MediationAdapter extends RtbAdapter {

    private String versionName = "";

    @Override
    public void collectSignals(@NonNull RtbSignalData rtbSignalData, @NonNull SignalCallbacks signalCallbacks) {
        try {
            signalCallbacks.onSuccess(UID2Manager.shared.getIdentity().getValue().getJsonString());
        } catch (Exception e) {
            signalCallbacks.onFailure(e.toString());
        }
    }

    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        return getVersionInfo(BuildConfig.GMS_LIB_VERSION);
    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {
        return getVersionInfo(versionName);
    }

    @Override
    public void initialize(@NonNull Context context, @NonNull InitializationCompleteCallback initializationCompleteCallback, @NonNull List<MediationConfiguration> list) {
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            // Invoke the InitializationCompleteCallback once initialization completes.
            initializationCompleteCallback.onInitializationSucceeded();
        } catch (Exception e) {
            initializationCompleteCallback.onInitializationFailed(e.getLocalizedMessage());
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
