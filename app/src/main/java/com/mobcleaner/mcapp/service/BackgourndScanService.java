package com.mobcleaner.mcapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.mobcleaner.mcapp.util.Utils;
import com.trustlook.sdk.Constants;
import com.trustlook.sdk.cloudscan.CloudScanClient;
import com.trustlook.sdk.cloudscan.CloudScanListener;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.Error;
import com.trustlook.sdk.data.Region;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class BackgourndScanService extends IntentService {
    Context context=this;

    public BackgourndScanService() {
        super("BackgourndScanService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
//            List<String> packageNameList = getAppList();
            final String packageName = intent.getStringExtra("package_name");
            Log.d(Constants.TAG,"Handle install intent with package name: "+packageName);
            CloudScanClient cloudScanClient = new CloudScanClient.Builder(context)
                    .setRegion(Region.INTL)
                    .setConnectionTimeout(Utils.CONNECTION_TIMEOUT)
                    .setSocketTimeout(Utils.SOCKET_TIMEOUT)
                    .build();

            cloudScanClient.startQuickScan(new CloudScanListener() {
                @Override
                public void onScanStarted() {
                    Log.d(Constants.TAG, "startQuickScan onScanStarted");
                }

                @Override
                public void onScanProgress(int curr, int total, AppInfo result) {
                    Log.d(Constants.TAG, "startQuickScan onScanProgress " + curr + "/" + total+" "+result.getPackageName());
                }

                @Override
                public void onScanError(int errCode, String message) {
                    Log.d(Constants.TAG, "startQuickScan onScanError " + errCode);
                    String ErrorMessage = "";

                    if (errCode == Error.HOST_NOT_DEFINED) {
                        ErrorMessage = "Error " + errCode + ": HOST_NOT_DEFINED";
                    } else if (errCode == Error.INVALID_INPUT) {
                        ErrorMessage = "Error  " + errCode + ": INVALID_INPUT, no samples to scan";
                    } else if (errCode == Error.SERVER_NOT_AVAILABLE) {
                        ErrorMessage = "Error " + errCode + ": SERVER_NOT_AVAILABLE";
                    } else if (errCode == Error.KEY_SERVER_NOT_AVAILABLE) {
                        ErrorMessage = "Error " + errCode + ": KEY_SERVER_NOT_AVAILABLE";
                    } else if (errCode == Error.SCAN_SERVER_NOT_AVAILABLE) {
                        ErrorMessage = "Error " + errCode + ": SCAN_SERVER_NOT_AVAILABLE";
                    } else if (errCode == Error.JSON_EXCEPTION) {
                        ErrorMessage = "Error " + errCode + ": JSON_EXCEPTION";
                    } else if (errCode == Error.IO_EXCEPTION) {
                        ErrorMessage = "Error " + errCode + ": IO_EXCEPTION";
                    } else if (errCode == Error.NO_NETWORK) {
                        ErrorMessage = "Error " + errCode + ": NO_NETWORK";
                    } else if (errCode == Error.SOCKET_TIMEOUT_EXCEPTION) {
                        ErrorMessage = "Error " + errCode + ": SOCKET_TIMEOUT_EXCEPTION";
                    } else if (errCode == Error.INVALID_KEY) {
                        ErrorMessage = "Error " + errCode + ": INVALID_KEY, please check the key in AndroidManifest.xml";
                    } else if (errCode == Error.UNSTABLE_NETWORK) {
                        ErrorMessage = "Error " + errCode + ": UNSTABLE_NETWORT";
                    } else if (errCode == Error.INVALID_SIGNATURE) {
                        ErrorMessage = "Error " + errCode + ": INVALID_SIGNATURE";
                    }else {
                        ErrorMessage = "Error " + errCode + " " + message;
                    }
                }

                @Override
                public void onScanCanceled() {
                    Toast.makeText(context, "startQuickScan onScanCanceled", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onScanInterrupt() {

                }

                @Override
                public void onScanFinished(List<AppInfo> results) {
                    Log.d(Constants.TAG, "startQuickScan onScanFinished " + results.size() + " APPs");
                }
            });
        }
    }

    private List<String> getAppList() {
        List<String> packageNameList = new ArrayList<String>();
        try {
            PackageManager packageManager = context.getPackageManager();
            Process process = Runtime.getRuntime().exec("pm list package");
            BufferedReader bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            int total_count = 0;
            while ((line = bis.readLine()) != null) {
                try {
                    String packageName = line.split("package:", 2)[1];
                    Log.d(Constants.TAG, "getAppList: " + total_count + " " + packageName);
                    total_count += 1;
                    PackageInfo pkgInfo = packageManager.getPackageInfo(packageName, 0);

                    if (packageName.equals(context.getPackageName())) {
                        // don't scan myself
                        Log.d(Constants.TAG, "getAppList Skip scan myself " + packageName);
                        continue;
                    }

                    if (isSystemApp(pkgInfo)) {
                        Log.d(Constants.TAG, "getAppList Skip scan system app: " + packageName);
                        continue;
                    }
                    packageNameList.add(packageName);
                } catch (Exception e) {
                    Log.d(Constants.TAG, "getAppList error1:"+e.getMessage());
                }
            }
        } catch(Exception e) {
            Log.e(Constants.TAG, "getAppList error2:"+e.getMessage());
        }
        Log.d(Constants.TAG, "Get "+packageNameList.size()+" apps to scan");
        return packageNameList;
    }

    public boolean isSystemApp(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

}