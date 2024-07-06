package com.mobcleaner.mcapp.Adapter;

import android.content.Context;
import androidx.core.content.res.ResourcesCompat;

import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobcleaner.mcapp.R;
import com.trustlook.sdk.data.AppInfo;
import com.trustlook.sdk.data.PkgInfo;

import java.util.List;
import java.util.Locale;

/**
 * Created by mulinli on 11/29/19.
 */
public class AppInfoAdaptor extends BaseAdapter {

    List<PkgInfo> pkgList;
    List<AppInfo> appInfoList;

    Context context;
    private final int VIEW_MODE_PKG_INFO = 1;
    private final int VIEW_MODE_APP_INFO = 2;
    int viewMode;

    class ViewHolder {
        private ImageView ivAppIcon;
        private TextView tvAppName;
        private TextView tvMD5;
        private TextView tvPkgName;
        private TextView tvPkgScore;
        private TextView tvVirusFamily;
        private TextView tvVirusCategory;
        private TextView tvSum;
        private TextView tvLocalRes;
    }

    public AppInfoAdaptor(Context context) {
        this.context = context;

    }

    public void setAppInfoList(List<AppInfo> appInfoList) {
        this.appInfoList = appInfoList;
        viewMode = VIEW_MODE_APP_INFO;
    }

    public void setPkgList(List<PkgInfo> pkgList) {
        this.pkgList = pkgList;
        viewMode = VIEW_MODE_PKG_INFO;
    }

    @Override
    public int getCount() {
        if(viewMode ==VIEW_MODE_PKG_INFO) {
            return pkgList.size();
        } else if(viewMode ==VIEW_MODE_APP_INFO ) {
            return appInfoList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.row_pkg_list, parent, false);
            holder = new ViewHolder();
            holder.tvAppName = (TextView) convertView.findViewById(R.id.tv_app_name);
            holder.tvMD5 = (TextView) convertView.findViewById(R.id.tv_md5);
            holder.tvPkgName = (TextView) convertView.findViewById(R.id.tv_pkg_name);
            holder.tvPkgScore = (TextView) convertView.findViewById(R.id.tv_pkg_score);
            holder.tvVirusFamily = (TextView) convertView.findViewById(R.id.tv_virus_family);
            holder.tvVirusCategory = (TextView) convertView.findViewById(R.id.tv_virus_category);
            holder.tvSum = (TextView) convertView.findViewById(R.id.tv_sum);
            holder.tvLocalRes = (TextView) convertView.findViewById(R.id.tv_local_res);
            holder.ivAppIcon = (ImageView) convertView.findViewById(R.id.iv_app_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

            if(viewMode== VIEW_MODE_PKG_INFO) {
                Object object = pkgList.get(position);
                if( null != object ) {
                    holder.tvPkgName.setText(((PkgInfo) object).getPkgName());

                }
            } else if (viewMode== VIEW_MODE_APP_INFO){
                AppInfo object = appInfoList.get(position);
                if( null != object ) {
                    holder.tvAppName.setText(object.getAppName());
                    holder.tvMD5.setText(context.getResources().getString(R.string.md5_label) + "  " + object.getMd5());
                    if (object.getPackageName() != null && !object.getPackageName().isEmpty()) {
                        holder.tvPkgName.setText(object.getPackageName());
                    } else {
                        holder.tvPkgName.setText(object.getApkPath());
                    }

                    if (object.getScore() >= 8) {
                        holder.tvPkgScore.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.colorDanger, null));
                    } else if (object.getScore() >=6){
                        holder.tvPkgScore.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.colorRisk, null));
                    } else {
                        holder.tvPkgScore.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.colorSafe, null));
                    }

                    holder.tvPkgScore.setText(context.getResources().getString(R.string.score_label) + "  " + String.valueOf(object.getScore()));
                    holder.tvVirusFamily.setText(context.getResources().getString(R.string.virus_label) + "  "+ object.getVirusName());

                    if (object.getCategory() != null) {
                        String virusCategory;
                        if (Locale.getDefault().getLanguage().equals("zh")) {
                            virusCategory = object.getCategory()[0];
                        } else {
                            virusCategory = object.getCategory()[1];
                        }
                        holder.tvVirusCategory.setText(context.getResources().getString(R.string.category) + "  " + virusCategory);
                    }

                    if (object.getSummary() != null) {
                        String summary;
                        if (Locale.getDefault().getLanguage().equals("zh")) {
                            summary = object.getSummary()[0];
                        } else {
                            summary = object.getSummary()[1];
                        }
                        holder.tvSum.setText(context.getResources().getString(R.string.summary) + "  " + summary);
                    }

                    String localRes = context.getResources().getString(R.string.static_or_dynamic) + " "
                            + (object.isFromStatic() ?
                            context.getResources().getString(R.string.static_engine) : context.getResources().getString(R.string.cloud));

                    holder.tvLocalRes.setText(localRes);

                    String packageName = object.getPackageName();
                    try {
                        Drawable icon = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.loadIcon(context.getPackageManager());
                        holder.ivAppIcon.setImageDrawable(icon);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.d("AppIcons","no icon");
                    }
                }
            }
        return convertView;
    }
}
