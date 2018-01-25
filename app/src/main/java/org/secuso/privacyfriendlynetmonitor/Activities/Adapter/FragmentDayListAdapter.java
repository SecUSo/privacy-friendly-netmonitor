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

package org.secuso.privacyfriendlynetmonitor.Activities.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.Activities.HistoryDetailActivity;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntity;
import org.secuso.privacyfriendlynetmonitor.R;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by m4rc0 on 14.01.2018.
 */

public class FragmentDayListAdapter extends RecyclerView.Adapter<FragmentDayListAdapter.ViewHolder> {

    List<ReportEntity> reportEntities;
    Context context;

    public FragmentDayListAdapter(List<ReportEntity> reportEntities, Context context) {
        this.reportEntities = reportEntities;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout relativeLayout;

        public ViewHolder(RelativeLayout itemView) {
            super(itemView);
            relativeLayout = itemView;
        }
    }

    @Override
    public FragmentDayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_item, parent, false);

        ViewHolder vh = new ViewHolder(relativeLayout);
        return vh;
    }

    @Override
    public void onBindViewHolder(FragmentDayListAdapter.ViewHolder holder, int position) {

        final ReportEntity reportEntity = reportEntities.get(position);

        TextView textViewAppName = holder.relativeLayout.findViewById(R.id.fragment_appname);
        TextView textViewTimestamp = holder.relativeLayout.findViewById(R.id.fragment_timestamp_value);
        TextView textViewConnectionInfo = holder.relativeLayout.findViewById(R.id.fragment_conncection_info_value);

        textViewAppName.setText(reportEntity.getRemoteAddress());
        textViewTimestamp.setText(reportEntity.getTimeStamp());
        textViewConnectionInfo.setText(reportEntity.getConnectionInfo());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> detailsList = prepareData(reportEntity);
                Intent intent = new Intent(context, HistoryDetailActivity.class);
                intent.putExtra("Details", (ArrayList) detailsList);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return reportEntities.size();
    }


    private List<String> prepareData(ReportEntity reportEntity) {

        PackageManager packageManager = context.getPackageManager();

        String details = "";
        List<String> detailsList = new ArrayList<String>();

        String appName = reportEntity.getAppName();
        detailsList.add(appName);

        String uid = reportEntity.getUserID();
        detailsList.add(uid);

        PackageInfo packageInfo = null;

        try {
            packageInfo = packageManager.getPackageInfo(appName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            System.out.println("Could not find package info for " + appName + ".");
        }

        details = packageInfo.versionName;
        detailsList.add(details);

        details = new Date(packageInfo.firstInstallTime).toString();
        detailsList.add(details);

        details = reportEntity.getRemoteAddress();
        detailsList.add(details);

        details = reportEntity.getRemoteHex();
        detailsList.add(details);

        details = reportEntity.getRemoteHost();
        detailsList.add(details);

        details = reportEntity.getLocalAddress();
        detailsList.add(details);

        details = reportEntity.getLocalHex();
        detailsList.add(details);

        details = reportEntity.getServicePort();
        detailsList.add(details);

        details = reportEntity.getPayloadProtocol();
        detailsList.add(details);

        details = reportEntity.getTransportProtocol();
        detailsList.add(details);

        details = reportEntity.getLocalPort();
        detailsList.add(details);

        details = reportEntity.getTimeStamp();
        detailsList.add(details);

        details = reportEntity.getConnectionInfo();
        detailsList.add(details);

        return detailsList;
    }
}
