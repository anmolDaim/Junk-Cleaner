package com.mobcleaner.mcapp.Fragment;

import android.content.Context;
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
import com.trustlook.sdk.Constants;


/**
 * Created by Lifan on 03/11/2021.
 */

public class ComprehensiveScanFragment extends Fragment{

    OnComprehensiveScanStartListener onComprehensiveScanStartListener;
    private RelativeLayout btScan;
    private Context context;

    TextView folderScanDesc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = this.getActivity();

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_customize_scan, container, false);

        String sdRoot = Environment.getExternalStorageDirectory().getAbsolutePath();

        folderScanDesc = rootView.findViewById(R.id.tv_folder_scan_desc);
        String formatted = context.getResources().getString(R.string.comprehensive_scan_desc, sdRoot);
        folderScanDesc.setText(formatted);

        btScan = rootView.findViewById(R.id.btn_scan);
        btScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Constants.TAG, "Comprehensive Scan ");
                onComprehensiveScanStartListener.onComprehensiveScanStartListener();
            }
        });
        return rootView;
    }

    public interface OnComprehensiveScanStartListener{
        void onComprehensiveScanStartListener();
    }


    @Override
    public void onAttach(Context context) {
        if (context instanceof OnComprehensiveScanStartListener) {
            onComprehensiveScanStartListener = (OnComprehensiveScanStartListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnComprehensiveScanStartListener");
        }
        super.onAttach(context);
    }

}
