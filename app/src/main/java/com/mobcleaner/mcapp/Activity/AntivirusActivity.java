package com.mobcleaner.mcapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.cleanmaster.junk.bean.SDcardRubbishResult;
import com.cloud.cleanjunksdk.ad.AdvBean;
import com.cloud.cleanjunksdk.cache.CacheBean;
import com.cloud.cleanjunksdk.filescan.ApkBean;
import com.cloud.cleanjunksdk.filescan.LogBean;
import com.cloud.cleanjunksdk.filescan.TmpBean;
import com.cloud.cleanjunksdk.residual.ResidualBean;
import com.cloud.cleanjunksdk.task.CheckSdkCallback;
import com.cloud.cleanjunksdk.task.Clean;
import com.cloud.cleanjunksdk.task.CleanSDK;
import com.cloud.cleanjunksdk.task.JunkScanCallback;
import com.cm.plugincluster.junkengine.junk.bean.MediaFile;
import com.cm.plugincluster.junkengine.junk.engine.MEDIA_TYPE;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobcleaner.mcapp.Fragment.CloudScanFragment;
import com.mobcleaner.mcapp.Fragment.CloudScanResultFragment;
import com.mobcleaner.mcapp.Fragment.ComprehensiveScanFragment;
import com.mobcleaner.mcapp.Fragment.FolderScanFragment;
import com.mobcleaner.mcapp.service.InstallReceiver;
import com.mobcleaner.mcapp.R;
import com.mobcleaner.mcapp.Fragment.SplashFragment;
import com.mobcleaner.mcapp.util.Utils;
import com.trustlook.sdk.BuildConfig;
import com.trustlook.sdk.Constants;
import com.trustlook.sdk.cloudscan.CloudScanClient;
import com.trustlook.sdk.cloudscan.CloudScanListener;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.Error;
import com.trustlook.sdk.data.Region;

import java.util.List;

import static android.os.Build.VERSION.SDK_INT;

public class AntivirusActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CloudScanFragment.OnQuickScanStartListener,
        FolderScanFragment.OnFolderScanStartListener,
        ComprehensiveScanFragment.OnComprehensiveScanStartListener {

    NavigationView navigationView;
    public Toolbar toolbar;
    CloudScanClient cloudScanClient;
    InstallReceiver installReceiver;
    Context context;

    Clean sdkClean;
    boolean isInitSuccess = false;
    JunkScanCallback junkScanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "Setup StrictMode");
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()   // or .detectAll() for all detectable problems
//                    .penaltyLog()
//                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
//                    .penaltyDeath()
                    .build());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antivirus);

        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        ImageView backBtn = toolbar.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AntivirusActivity.super.onBackPressed();
            }
        });

        ImageView menuBtn = toolbar.findViewById(R.id.ivMenu);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });

        setSupportActionBar(toolbar);

        // Set toolbar navigation icon color
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }

// Set toolbar menu icon color
        for (int i = 0; i < toolbar.getMenu().size(); i++) {
            Drawable drawable = toolbar.getMenu().getItem(i).getIcon();
            if (drawable != null) {
                drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            }
        }

        launchSplash();

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        cloudScanClient = new CloudScanClient.Builder(context)
                .setRegion(Region.INTL)
                .setConnectionTimeout(Utils.CONNECTION_TIMEOUT)
                .setSocketTimeout(Utils.SOCKET_TIMEOUT)
                .build();


//        initCleanSDK();

        boolean getPermission = requestForPermission(context);
        if (getPermission) {
            finishSplash();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(installReceiver == null) {
            Log.d(Constants.TAG, "Register install receiver");
            installReceiver = new InstallReceiver();
            IntentFilter filter = new IntentFilter();

            filter.addAction("android.intent.action.PACKAGE_ADDED");
            filter.addDataScheme("package");

            this.registerReceiver(installReceiver, filter);
        }
    }

    @Override
    public void onDestroy(){
        if(installReceiver != null) {
            Log.d(Constants.TAG, "Unregister install receiver");
            this.unregisterReceiver(installReceiver);
        }

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utils.EXTERNAL_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    finishSplash();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(context, R.string.need_permission, Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onBackPressed() {
        cloudScanClient.cancelScan();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.flContent);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass = null;
        TextView categoryTextView = findViewById(R.id.category);
        if (id == R.id.nav_cloud_scan) {
            fragmentClass = CloudScanFragment.class;
            categoryTextView.setText("Quick Scan");
        } else if (id == R.id.nav_folder_scan) {
            fragmentClass = FolderScanFragment.class;
            categoryTextView.setText("Folder Scan");
        } else if (id == R.id.nav_comprehensive_scan) {
            fragmentClass = ComprehensiveScanFragment.class;
            categoryTextView.setText("Comprehensive Scan");
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        item.setChecked(true);

        setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void launchSplash() {

        toolbar.setVisibility(View.INVISIBLE);

        Fragment fragment = null;
        Class fragmentClass = SplashFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

    }

    private void finishSplash() {
        new Handler().post(new Runnable() {
            public void run() {
                CloudScanFragment fragment = new CloudScanFragment();
                // Insert the fragment by replacing any existing fragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().addToBackStack(null).add(R.id.flContent, fragment).commit();
                navigationView.setCheckedItem(R.id.nav_cloud_scan);
                navigationView.getMenu().performIdentifierAction(R.id.nav_cloud_scan, 0);
                toolbar.setVisibility(View.VISIBLE);
            }
        });
    }

    public void packagenameScan() {
        AppInfo appinfo = cloudScanClient.packageNameScan("com.twitter.android", 10000);
        if(appinfo == null) {
            Log.d(Constants.TAG, "Failed to scan package name ");
        } else{
            Log.d(Constants.TAG, "result: " + appinfo.toString());
        }
        appinfo = cloudScanClient.packageNameScan("com.facebook.lite", 10000);
        if(appinfo == null) {
            Log.d(Constants.TAG, "Failed to scan package name ");
        } else{
            Log.d(Constants.TAG, "result: " + appinfo.toString());
        }
    }

    @Override
    public void onComprehensiveScanStartListener() {
        final CloudScanResultFragment fragment = new CloudScanResultFragment();
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(null).add(R.id.flContent, fragment).commit();
        cloudScanClient.startComprehensiveScan(new CloudScanListener() {
            @Override
            public void onScanStarted() {
                Log.d(Constants.TAG, "startComprehensiveScan onScanStarted");
            }

            @Override
            public void onScanProgress(int curr, int total, AppInfo result) {
                Log.d(Constants.TAG, "startComprehensiveScan onScanProgress " + curr + "/" + total);
                fragment.tvProgress.setText(result.getApkPath());
            }

            @Override
            public void onScanError(int errCode, String message) {

                Log.d(Constants.TAG, "ComprehensiveScan onScanError " + errCode);
                String ErrorMessage = "";

                if (errCode == Error.HOST_NOT_DEFINED) {
                    ErrorMessage = "Error " + errCode + ": HOST_NOT_DEFINED";
                }
                else if (errCode == Error.INVALID_INPUT) {
                    ErrorMessage = "Error " + errCode + ": INVALID_INPUT, no samples to scan";
                }
                else if (errCode == Error.SERVER_NOT_AVAILABLE) {
                    ErrorMessage = "Error " + errCode + ": SERVER_NOT_AVAILABLE, please check the key in AndroidManifest.xml";
                }
                else if (errCode == Error.JSON_EXCEPTION) {
                    ErrorMessage = "Error " + errCode + ": JSON_EXCEPTION";
                }
                else if (errCode == Error.IO_EXCEPTION) {
                    ErrorMessage = "Error " + errCode + ": IO_EXCEPTION";
                }
                else if (errCode == Error.NO_NETWORK) {
                    ErrorMessage = "Error " + errCode + ": NO_NETWORK";
                }
                else if (errCode == Error.SOCKET_TIMEOUT_EXCEPTION) {
                    ErrorMessage = "Error " + errCode + ": SOCKET_TIMEOUT_EXCEPTION";
                }
                else if (errCode == Error.INVALID_KEY) {
                    ErrorMessage = "Error " + errCode + ": INVALID_KEY, please check the key in AndroidManifest.xml";
                }
                else if (errCode == Error.UNSTABLE_NETWORK) {
                    ErrorMessage = "Error " + errCode + ": UNSTABLE_NETWORT";
                }
                else {
                    ErrorMessage = "Error " + errCode + " " + message;
                }

                fragment.tvProgress.setText(ErrorMessage);
                fragment.rlScanning.setVisibility(View.GONE);
            }

            @Override
            public void onScanCanceled() {
                Toast.makeText(context, "startComprehensiveScan onScanCanceled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onScanInterrupt() {

            }

            @Override
            public void onScanFinished(List<AppInfo> results) {
                Log.d(Constants.TAG, "startComprehensiveScan onScanFinished " + results.size() + " APKs");
                fragment.update(results, results.size());
            }
        });
    }

    @Override
    public void onQuickScanStartListener() {

        final CloudScanResultFragment fragment = new CloudScanResultFragment();

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(null).add(R.id.flContent, fragment).commit();

//        cloudScanClient.startPackagenameScan("com.twitter.android", new CloudScanListener() {
        cloudScanClient.startQuickScan(new CloudScanListener() {
            @Override
            public void onScanStarted() {
                Log.d(Constants.TAG, "startQuickScan onScanStarted");
            }

            @Override
            public void onScanProgress(int curr, int total, AppInfo result) {
                Log.d(Constants.TAG, "startQuickScan onScanProgress " + curr + "/" + total);
                if (!result.getPackageName().isEmpty()) {
                    fragment.tvProgress.setText(result.getPackageName());
                } else if (!result.getApkPath().isEmpty()) {
                    fragment.tvProgress.setText(result.getApkPath());
                } else {
                    fragment.tvProgress.setText(R.string.scanning);

                }
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

                fragment.tvProgress.setText(ErrorMessage);
                fragment.rlScanning.setVisibility(View.GONE);

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
                fragment.update(results, results.size());
//                packagenameScan();
            }
        });

        // cleanSDK demo
//        startScans();
    }

    @Override
    public void onFolderScanStartListener(List<String> folderDir) {
        final CloudScanResultFragment fragment = new CloudScanResultFragment();
        final int totalFile = folderDir.size();
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(null).add(R.id.flContent, fragment).commit();
//        cloudScanClient.startEicarScan(folderDir, new CloudScanListener() {
        cloudScanClient.startFolderScan(folderDir, new CloudScanListener() {
            @Override
            public void onScanStarted() {
                Log.d(Constants.TAG, "startFolderScan onScanStarted");
            }

            @Override
            public void onScanProgress(int curr, int total, AppInfo result) {
                Log.d(Constants.TAG, "startFolderScan onScanProgress " + curr + "/" + total);
                fragment.tvProgress.setText(result.getApkPath());
            }

            @Override
            public void onScanError(int errCode, String message) {

                Log.d(Constants.TAG, "FolderScan onScanError " + errCode);
                String ErrorMessage = "";

                if (errCode == Error.HOST_NOT_DEFINED) {
                    ErrorMessage = "Error " + errCode + ": HOST_NOT_DEFINED";
                } else if (errCode == Error.INVALID_INPUT) {
                    ErrorMessage = "Error " + errCode + ": INVALID_INPUT, no samples to scan";
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
                } else {
                    ErrorMessage = "Error " + errCode + " " + message;
                }
                fragment.tvProgress.setText(ErrorMessage);
                fragment.rlScanning.setVisibility(View.GONE);


            }

            @Override
            public void onScanCanceled() {
                Toast.makeText(context, "startFolderScan onScanCanceled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onScanInterrupt() {

            }

            @Override
            public void onScanFinished(List<AppInfo> results) {
                Log.d(Constants.TAG, "startFolderScan onScanFinished " + results.size() + " APKs");
                fragment.update(results, totalFile);
            }
        });

    }

    public boolean requestForPermission(Context context) {

        boolean isPermissionOn = true;

        if (SDK_INT >= 30 && (!Environment.isExternalStorageManager())) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        }

        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 30) {
              if (!Utils.canAccessExternalSd(context)) {
                    isPermissionOn = false;
                    requestPermissions(Utils.EXTERNAL_STORAGE_PERMS, Utils.EXTERNAL_REQUEST);
              }
        }

        return isPermissionOn;
    }

    public void initCleanSDK() {
        CleanSDK.init(null, context, com.cloud.cleanjunksdk.tools.Region.INTL, new CheckSdkCallback() {
            @Override
            public void onSuccess(Clean clean) {
                isInitSuccess = true;
                Log.d(Constants.TAG, "################initCleanSDK onSuccess ");
                sdkClean = clean;
                initJunkScanCallBack();
//                initSimilarScanCallBack();
//                initBigFileCallBack();
//                finishSplash();
            }

            @Override
            public void onError(int errCode) {
                Log.d(Constants.TAG, "################initCleanSDK onError "+errCode);
                Toast.makeText(context, "Initialization failed with error "+errCode, Toast.LENGTH_LONG).show();
//                pv_main.setVisibility(View.GONE);


            }
        });//initialize SDK
    }

    private void startScans (){
        sdkClean.startScan(junkScanCallback, true);
//            sdkClean.startAPKScan(junkScanCallback);
//            sdkClean.startLogScan(junkScanCallback);
//            sdkClean.startTmpScan(junkScanCallback);
//            sdkClean.startAdScan(junkScanCallback);
//            sdkClean.startResidualScan(junkScanCallback);
//            sdkClean.startAppCacheScan(junkScanCallback);
//            sdkClean.startPhotoScan(junkScanCallback);
//            sdkClean.startScreenshotScan(junkScanCallback);
//            sdkClean.startVideoScan(junkScanCallback);
//            sdkClean.startAudioScan(junkScanCallback);
//            sdkClean.startThumbnailScan(junkScanCallback);
//            sdkClean.startDocScan(junkScanCallback);
    }

    private void initJunkScanCallBack() {

        junkScanCallback = new JunkScanCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void error(int errorCode, Throwable throwable) {
                Log.d(Constants.TAG, "error" + errorCode);
            }

            @Override
            public void onAdJunkEmitOne(AdvBean advBean) {
            }

            @Override
            public void onAdJunkSucceed() {
                Log.d(Constants.TAG, "onAdJunkSucceed");
            }

            @Override
            public void onApkJunkEmitOne(ApkBean fileBean) {
                Log.d(Constants.TAG, "onApkJunkEmitOne");
            }

            @Override
            public void onApkJunkScanSucceed() {
                Log.d(Constants.TAG, "onApkJunkScanSucceed");
            }

            @Override
            public void onTmpJunkEmitOne(TmpBean fileBean) {
                Log.d(Constants.TAG, "onTmpJunkEmitOne");
            }

            @Override
            public void onTmpJunkScanSucceed() {
                Log.d(Constants.TAG, "onTmpJunkScanSucceed");
            }

            @Override
            public void onLogJunkEmitOne(LogBean fileBean) {
                Log.d(Constants.TAG, "onLogJunkEmitOne");
            }

            @Override
            public void onLogJunkScanSucceed() {
                Log.d(Constants.TAG, "onLogJunkScanSucceed");
            }

            @Override
            public void onCacheJunkEmitOne(CacheBean routeBeen) {
                Log.d(Constants.TAG, "onCacheJunkEmitOne routeBeen:" + routeBeen);
            }

            @Override
            public void onCacheJunkSucceed() {
                Log.d(Constants.TAG, "onCacheJunkSucceed");
            }

            @Override
            public void onResidualEmitOne(ResidualBean residualBeen) {
                Log.d(Constants.TAG, "onResidualEmitOne");
            }

            @Override
            public void onResidualJunkSucceed() {
                Log.d(Constants.TAG, "onResidualJunkSucceed");
            }

            @Override
            public void onTimeOut() {
                Log.d(Constants.TAG, "onTimeOut");
            }

            @Override
            public void onThumbnailJunkEmitOne(SDcardRubbishResult sDcardRubbishResult) {
                Log.d(Constants.TAG, "onThumbnailJunkEmitOne sDcardRubbishResult:" + sDcardRubbishResult.getStrDirPath());
            }

            @Override
            public void onThumbnailJunkScanSucceed() {
                Log.d(Constants.TAG, "onThumbnailJunkScanSucceed");
            }

            @Override
            public void onMediaFileJunkEmitOne(MEDIA_TYPE type, MediaFile mediaFile) {
//             MEDIA_TYPE.PHOTO, MEDIA_TYPE.AUDIO, MEDIA_TYPE.VIDEO, MEDIA_TYPE.SCREENSHOT, MEDIA_TYPE.DOC
                Log.d(Constants.TAG, "onMediaFileJunkEmitOne mediaFile:" + mediaFile.getPath() + ",type:" + type);
            }

            @Override
            public void onMediaFileJunkScanSucceed(MEDIA_TYPE type) {
                Log.d(Constants.TAG, "onMediaFileJunkScanSucceed " + type.toString());
            }

        };
    }

}
