package com.uid2.client;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;
import com.uid2.client.UID2Client;

public class UID2ManagerInitializer implements Initializer {
    @NonNull
    @Override
    public Object create(@NonNull Context context) {
        return UID2Manager.getInstance(context);
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
