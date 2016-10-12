package com.sjsu.securephone.theftdetector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";

    public BootCompleteReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Boot completed. Main Activity will start now.");
            Intent in = new Intent(context, MainActivity.class);
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(in);

    }
}
