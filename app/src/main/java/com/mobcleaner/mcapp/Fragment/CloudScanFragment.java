package com.mobcleaner.mcapp.Fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mobcleaner.mcapp.R;

/**
 * Created by mulinli on 11/28/17.
 */

public class CloudScanFragment extends Fragment{

    Context context;
    RelativeLayout rlStart;
    OnQuickScanStartListener onQuickScanStartListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = this.getActivity();

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_cloud_scan, container, false);

        rlStart = rootView.findViewById(R.id.btn_scan);
        rlStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onQuickScanStartListener.onQuickScanStartListener();
            }
        });
        return rootView;
    }


    public interface OnQuickScanStartListener{
        void onQuickScanStartListener();
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof OnQuickScanStartListener) {
            onQuickScanStartListener = (OnQuickScanStartListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnQuickScanStartListener");
        }
        super.onAttach(context);
    }
}
