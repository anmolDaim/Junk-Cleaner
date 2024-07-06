package com.mobcleaner.mcapp.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobcleaner.mcapp.Adapter.AppInfoAdaptor;
import com.mobcleaner.mcapp.R;
import com.trustlook.sdk.data.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by mulinli on 11/28/17.
 */

public class CloudScanResultFragment extends Fragment{

    Context context;
    public RelativeLayout rlScanning;
    RelativeLayout rlResult;
    public TextView tvProgress;
    TextView tvTotal;
    TextView tvInvalid;
    TextView tvMalware;
    TextView tvPUA;
    TextView tvBenign;
    TextView tvTimeCost;
    List<AppInfo> appInfoList = new ArrayList<AppInfo>();
    ListView lvApp;
    AppInfoAdaptor adaptor;

    long startTime, millis;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = this.getActivity();

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_cloud_scan_result, container, false);

        rlScanning = rootView.findViewById(R.id.rl_scanning);
        tvProgress = rootView.findViewById(R.id.tv_progressing);

        rlResult = rootView.findViewById(R.id.rl_result);
        lvApp = rootView.findViewById(R.id.list);
        setListViewHeader();
        startTime = System.currentTimeMillis();

        return rootView;
    }

    private void setListViewHeader() {

        View header = View.inflate(context, R.layout.listview_header, null);

        tvTotal = header.findViewById(R.id.tv_scan_total_count);
        tvInvalid = header.findViewById(R.id.tv_scan_invalid_count);
        tvMalware = header.findViewById(R.id.tv_scan_malware_count);
        tvPUA = header.findViewById(R.id.tv_scan_pua_count);
        tvBenign = header.findViewById(R.id.tv_scan_benign_count);
        tvTimeCost = header.findViewById(R.id.tv_scan_time_count);
        lvApp.addHeaderView(header);
    }

    private String convertToTime(long millis) {
        if (millis < 1000) {
            return "00:00:01";
        }
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public void update(List<AppInfo> appInfoList, int totalFile) {
        millis = System.currentTimeMillis() - startTime;
        int invalid = totalFile - appInfoList.size();
        if (invalid<=0) {
            invalid=0;
        }
        int malware = 0;
        int pua = 0;
        int benign = 0;


        if (appInfoList.size() > 0){
            Collections.sort(appInfoList);
            for (AppInfo appInfo: appInfoList) {
                if (appInfo.getScore() >= 8) {
                    malware ++;
                } else if (appInfo.getScore() >= 6) {
                    pua ++;
                } else {
                    benign ++;
                }
            }
        }
        adaptor = new AppInfoAdaptor(context);
        adaptor.setAppInfoList(appInfoList);
        lvApp.setAdapter(adaptor);
        tvProgress.setVisibility(View.GONE);
        rlScanning.setVisibility(View.GONE);
        rlResult.setVisibility(View.VISIBLE);
        tvTotal.setText(String.valueOf(Math.max(appInfoList.size(),totalFile)));
        tvInvalid.setText(String.valueOf(invalid));
        tvMalware.setText(String.valueOf(malware));
        tvPUA.setText(String.valueOf(pua));
        tvBenign.setText(String.valueOf(benign));
        tvTimeCost.setText(convertToTime(millis));
    }

}
