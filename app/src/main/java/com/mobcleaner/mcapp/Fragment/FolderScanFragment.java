package com.mobcleaner.mcapp.Fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobcleaner.mcapp.R;
import com.mobcleaner.mcapp.util.Utils;
import com.trustlook.sdk.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by mulinli on 11/28/17.
 */

public class FolderScanFragment extends Fragment{

    OnFolderScanStartListener onFolderScanStartListener;
    private RelativeLayout btScan;
    private Context context;

    TextView folderScanDesc;

    private List<String> apkFilePathList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = this.getActivity();

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_customize_scan, container, false);

        String sdRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        File sdRootDir = new File(sdRoot+File.separator+"Download");
        apkFilePathList.clear();
        boolean getPermission = requestForPermission();
        if (getPermission) {
            Log.d(Constants.TAG, "Folder Scan " + sdRootDir.getAbsolutePath());
            getFileName(sdRootDir.listFiles());
        }

        folderScanDesc = rootView.findViewById(R.id.tv_folder_scan_desc);
        String formatted = context.getResources().getString(R.string.folder_scan_desc, sdRoot, apkFilePathList.size());
        folderScanDesc.setText(formatted);

        btScan = rootView.findViewById(R.id.btn_scan);
        btScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Constants.TAG, "Folder Scan " + apkFilePathList.size() + " apk fils");
                apkFilePathList.add(sdRootDir.getAbsolutePath());
                onFolderScanStartListener.onFolderScanStartListener(apkFilePathList);
            }
        });
        return rootView;
    }

    public interface OnFolderScanStartListener{
        void onFolderScanStartListener(List<String> folder);
    }


    @Override
    public void onAttach(Context context) {
        if (context instanceof OnFolderScanStartListener) {
            onFolderScanStartListener = (OnFolderScanStartListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFolderScanStartListener");
        }
        super.onAttach(context);
    }

    private void getFileName(File[] files) {
        if (files != null) {
            for (File file : files) {
//                Log.d(Constants.TAG, "getFileName " + file.getAbsolutePath());
                if (file.isDirectory()) {
                    getFileName(file.listFiles());
                } else {
                    String fileName = file.getName();

                    if (fileName.endsWith(".apk")) {
                        apkFilePathList.add(file.getPath());
                    }
                }

            }
        }
    }

    public boolean requestForPermission() {

        boolean isPermissionOn = true;

        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 30) {
            if (!Utils.canAccessExternalSd(context)) {
                isPermissionOn = false;
                requestPermissions(Utils.EXTERNAL_PERMS, Utils.EXTERNAL_REQUEST);
            }
        }

        return isPermissionOn;
    }

}
