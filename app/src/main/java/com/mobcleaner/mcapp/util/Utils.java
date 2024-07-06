package com.mobcleaner.mcapp.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.trustlook.sdk.Constants;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mulinli on 11/29/17.
 */

public class Utils {

    public static final String TAG = "TL";

    public static final int CONNECTION_TIMEOUT = 30000;
    public static final int SOCKET_TIMEOUT = 30000;

    public static final int EXTERNAL_REQUEST = 138;
    public static final String[] EXTERNAL_PERMS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static final String[] EXTERNAL_STORAGE_PERMS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static List<PackageInfo> getInstalledAppsPkgInfo(Context context) {
        List<PackageInfo> mPackageInfoList = new ArrayList<PackageInfo>();
        try {
            List<PackageInfo> packageInfoList = context.getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
            for (PackageInfo pi : packageInfoList) {

                if (pi != null && pi.applicationInfo != null) {
                    if (!isSystemPackage(pi)) {
                        mPackageInfoList.add(pi);
                    }
                }
            }
            Log.d(Constants.TAG, "=> Total installed packages: " + mPackageInfoList.size());
            return mPackageInfoList;

        }catch(RuntimeException re){

        }
        return new ArrayList<PackageInfo>();
    }

    public static boolean isSystemPackage(PackageInfo packageInfo) {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public static void writeToSD(String line, boolean appendMode){
        String filePath = "/sdcard/trustlook.csv";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath, appendMode);
            fileWriter.write(line);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean canAccessExternalSd(Context context) {
        return (hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private static boolean hasPermission(Context context, String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, perm));

    }
}