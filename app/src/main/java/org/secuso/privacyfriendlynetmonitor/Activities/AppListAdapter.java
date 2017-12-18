package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.R;

import java.util.List;

/**
 * Created by tobias on 08.12.17.
 */

public class AppListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<String> listStorage;
    private Context context;

    static class ViewHolder {
        SwitchCompat s;
        String appName;
    }

    public AppListAdapter(Context context, List<String> customizedListView) {
        layoutInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = customizedListView;
        this.context = context;
    }

    @Override
    public int getCount() {
        return listStorage.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.app_list_group, null);
        }

        final org.secuso.privacyfriendlynetmonitor.Activities.AppListAdapter.ViewHolder holder = new ViewHolder();
        holder.s = (SwitchCompat) convertView.findViewById(R.id.switchAppOnOffHistory);
        holder.s.setTag(position);
        holder.s.setOnCheckedChangeListener(null);

        String appName = listStorage.get(position);
        holder.appName = appName;

        if(Collector.getAppsToIncludeInScan().contains(appName)){
            holder.s.setChecked(true);
        }else{
            holder.s.setChecked(false);
        }

        TextView appGroupTitle = (TextView) convertView.findViewById(R.id.appGroupTitle);

        PackageManager packageManager = context.getPackageManager();

        try {
            appGroupTitle.setText((String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(appName, PackageManager.GET_META_DATA)));
        } catch (PackageManager.NameNotFoundException e) {
            appGroupTitle.setText(appName);
        }

        ImageView imgView = (ImageView) convertView.findViewById(R.id.appGroupIcon);

        try {
            imgView.setImageDrawable(packageManager.getApplicationIcon(appName));
        } catch (PackageManager.NameNotFoundException e) {}

        selectionHandling(holder);

        return convertView;
    }

    private void selectionHandling(final ViewHolder holder){

        holder.s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked )
                {
                    //System.out.println("True" + holder.appName); //TODO delete
                    Collector.addAppToIncludeInScan(holder.appName);
                }
                else
                {
                    //System.out.println("False"); //TODO delete
                    Collector.deleteAppFromIncludeInScan(holder.appName);
                }
            }
        });
    }


//    static class ViewHolder{
//
//        TextView textInListView;
//        ImageView imageInListView;
//    }
}
