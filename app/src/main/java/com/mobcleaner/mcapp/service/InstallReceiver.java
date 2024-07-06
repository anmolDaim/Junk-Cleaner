package com.mobcleaner.mcapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.trustlook.sdk.Constants;

public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            if (intent.getData() != null) {
                String packageName = intent.getDataString();
                Log.d(Constants.TAG, "Received intent of new package: " + packageName);
                Intent scanIntent = new Intent(context, BackgourndScanService.class);
                scanIntent.putExtra("package_name", packageName);

                context.startService(scanIntent);
            }
        }
    }
}
