package org.secuso.privacyfriendlynetmonitor.Activities.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.R;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by tobias on 09.03.18.
 */

public class AppListRecyclerAdapter extends RecyclerView.Adapter<AppListRecyclerAdapter.ViewHolder>{

    private List<String> app_list_name;

    private Context context;


    private SharedPreferences selectedAppsPreferences;
    private SharedPreferences.Editor editor;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView appGroupTitle;
        public TextView appInstallDate;
        public ImageView appIcon;
        public SwitchCompat appSwitch;

        public String appFullName;


        public ViewHolder(View view) {
            super(view);
            this.appGroupTitle = (TextView) view.findViewById(R.id.appGroupTitle);
            this.appInstallDate = (TextView) view.findViewById(R.id.appInstalledOn);
            this.appIcon = (ImageView) view.findViewById(R.id.appGroupIcon);
            this.appSwitch = (SwitchCompat) view.findViewById(R.id.switchAppOnOffHistory);
            this.appFullName = "";

        }
    }

    public AppListRecyclerAdapter(List<String> app_list_name, Context context) {
        this.app_list_name = app_list_name;
        this.context = context;

        selectedAppsPreferences = context.getSharedPreferences("SELECTEDAPPS", 0);
        editor = selectedAppsPreferences.edit();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_list_group, parent, false);
        return new ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
          String appName = app_list_name.get(position);
          holder.appFullName = appName;

        if (Collector.getAppsToIncludeInScan().contains(holder.appFullName)) {
            holder.appSwitch.setChecked(true);
        } else {
            holder.appSwitch.setChecked(false);
        }

        PackageManager packageManager = context.getPackageManager();

        try {
            holder.appGroupTitle.setText((String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(appName, PackageManager.GET_META_DATA)));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd. MMM yyyy, HH:mm");
            Date date = new Date(packageManager.getPackageInfo(appName, 0).firstInstallTime);
            holder.appInstallDate.setText("Installed:  " + simpleDateFormat.format(date));

            holder.appIcon.setImageDrawable(packageManager.getApplicationIcon(appName));
        } catch (PackageManager.NameNotFoundException e) {

        }

        holder.appSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {

                String appName = holder.appFullName;
                if (bChecked) {
                    if (!Collector.getAppsToIncludeInScan().contains(appName)) {
                        Collector.addAppToIncludeInScan(appName);
                        editor.putString(appName, appName);
                        editor.commit();
                        holder.appSwitch.setChecked(true);
                    }
                } else {
                    if (Collector.getAppsToIncludeInScan().contains(appName)) {
                        Collector.deleteAppFromIncludeInScan(appName);
                        editor.remove(appName);
                        editor.commit();
                        holder.appSwitch.setChecked(false);
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return app_list_name.size();
    }


}


