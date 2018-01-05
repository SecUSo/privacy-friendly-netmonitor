/*
    Privacy Friendly Net Monitor (Net Monitor)
    - Copyright (2015 - 2017) Felix Tsala Schiller

    ###################################################################

    This file is part of Net Monitor.

    Net Monitor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Net Monitor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Net Monitor.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von Net Monitor.

    Net Monitor ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Net Monitor wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.

    ###################################################################

    This app has been created in affiliation with SecUSo-Department of Technische Universität
    Darmstadt.

    The design is based on the Privacy Friendly Example App template by Karola Marky, Christopher
    Beckmann and Markus Hau (https://github.com/SecUSo/privacy-friendly-app-example).

    Privacy Friendly Net Monitor is based on TLSMetric by Felix Tsala Schiller
    https://bitbucket.org/schillef/tlsmetric/overview.

 */
package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.R;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tobias on 08.12.17.
 * Adapter displays the content of the App List for the History
 */

public class AppListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<String> listStorage;
    private Context context;
    private SharedPreferences selectedAppsPreferences;
    private SharedPreferences.Editor editor;

    private List<Integer> appsToDelete;

    static class ViewHolder {
        SwitchCompat s;
        String appName;
    }

    public AppListAdapter(Context context, List<String> customizedListView) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = customizedListView;
        this.context = context;
        appsToDelete = new ArrayList<Integer>();

        this.appsToDelete = appsToDelete;

        selectedAppsPreferences = context.getSharedPreferences("SELECTEDAPPS", 0);
        editor = selectedAppsPreferences.edit();
    }

    @Override
    public int getCount() {
        return listStorage.size();
    }

    @Override
    public Object getItem(int position) {
        return listStorage.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.app_list_group, null);
        }

        final View finalConvertView = convertView;

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appsToDelete.contains(position)) {
                    appsToDelete.remove(appsToDelete.indexOf(position));
                    RelativeLayout relativeLayout = (RelativeLayout) finalConvertView.findViewById(R.id.selectionApp);
                    relativeLayout.setBackgroundColor(Color.WHITE);
                }
            }
        });

        final View finalConvertView1 = convertView;
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!appsToDelete.contains(position)) {
                    appsToDelete.add(position);
                    RelativeLayout relativeLayout = (RelativeLayout) finalConvertView1.findViewById(R.id.selectionApp);
                    relativeLayout.setBackgroundColor(Color.CYAN);
                }

                return true;
            }
        });

        final org.secuso.privacyfriendlynetmonitor.Activities.AppListAdapter.ViewHolder holder = new ViewHolder();
        holder.s = (SwitchCompat) convertView.findViewById(R.id.switchAppOnOffHistory);
        holder.s.setTag(position);
        holder.s.setOnCheckedChangeListener(null);

        String appName = listStorage.get(position);
        holder.appName = appName;

        if (Collector.getAppsToIncludeInScan().contains(appName)) {
            holder.s.setChecked(true);
        } else {
            holder.s.setChecked(false);
        }

        TextView appGroupTitle = (TextView) convertView.findViewById(R.id.appGroupTitle);
        TextView appInstalledOn = (TextView) convertView.findViewById(R.id.appInstalledOn);

        PackageManager packageManager = context.getPackageManager();

        try {
            appGroupTitle.setText((String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(appName, PackageManager.GET_META_DATA)));
        } catch (PackageManager.NameNotFoundException e) {
            appGroupTitle.setText(appName);
        }

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd. MMM yyyy, HH:mm");
            Date date = new Date(packageManager.getPackageInfo(appName, 0).firstInstallTime);
            appInstalledOn.setText("Installed:  " + simpleDateFormat.format(date));
        } catch (PackageManager.NameNotFoundException e) {
            appInstalledOn.setText("");
        }

        ImageView imgView = (ImageView) convertView.findViewById(R.id.appGroupIcon);

        try {
            imgView.setImageDrawable(packageManager.getApplicationIcon(appName));
        } catch (PackageManager.NameNotFoundException e) {
        }

        selectionHandling(holder);

        return convertView;
    }

    private void selectionHandling(final ViewHolder holder) {

        holder.s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!Collector.getAppsToIncludeInScan().contains(holder.appName)) {
                        Collector.addAppToIncludeInScan(holder.appName);
                        editor.putString(holder.appName, holder.appName);
                        editor.commit();
                        holder.s.setChecked(true);
                    }
                } else {
                    if (Collector.getAppsToIncludeInScan().contains(holder.appName)) {
                        Collector.deleteAppFromIncludeInScan(holder.appName);
                        editor.remove(holder.appName);
                        editor.commit();
                        holder.s.setChecked(false);
                    }
                }
            }
        });
    }

    public List<Integer> getAppsToDelete() {
        return appsToDelete;
    }

}
