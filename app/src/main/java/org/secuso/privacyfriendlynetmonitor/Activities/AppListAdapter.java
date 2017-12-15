package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.R;

import java.sql.SQLOutput;
import java.util.List;

/**
 * Created by tobias on 08.12.17.
 */

public class AppListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<String> listStorage;
    private Context context;

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

        String appName = listStorage.get(position);
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


        return convertView;
    }


    static class ViewHolder{

        TextView textInListView;
        ImageView imageInListView;
    }
}
