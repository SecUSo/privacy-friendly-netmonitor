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
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntity;
import org.secuso.privacyfriendlynetmonitor.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by m4rc0 on 04.12.2017.
 * Adapter to display the Apps in the history
 */

public class ExpandableHistoryListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> uidList;
    private HashMap<String, List<ReportEntity>> reportListDetail;

    ExpandableHistoryListAdapter(Context context, List<String> uidList,
                                 HashMap<String, List<ReportEntity>> reportListDetail){
        this.context = context;
        this.uidList = uidList;
        this.reportListDetail = reportListDetail;

    }

    /**
     *
     * @param groupPosition
     * @param childPosititon
     * @return child
     */
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.reportListDetail.get(this.uidList.get(groupPosition))
                .get(childPosititon);
    }

    /**
     *
     * @param groupPosition
     * @param childPosition
     * @return child id
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     *
     * @param groupPosition
     * @param childPosition
     * @param isLastChild
     * @param convertView
     * @param parent
     * @return chield view
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ReportEntity reportEntity = ((ReportEntity) getChild(groupPosition, childPosition));
        final String dnsHostName = reportEntity.getRemoteHost();

        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.history_list_item, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.history_item_1);
        txtListChild.setText(dnsHostName);

        TextView history_item_2_type = (TextView) convertView.findViewById(R.id.history_item_2_type);
        history_item_2_type.setText("Time Stamp: ");

        TextView history_item_2_val = (TextView) convertView.findViewById(R.id.history_item_2_val);
        history_item_2_val.setText(reportEntity.getTimeStamp());

        return convertView;
    }

    /**
     *
     * @param groupPosition
     * @return children count
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return this.reportListDetail.get(this.uidList.get(groupPosition)).size();
    }

    /**
     *
     * @param groupPosition
     * @return group
     */
    @Override
    public Object getGroup(int groupPosition) {
        return this.reportListDetail.get(groupPosition);
    }


    /**
     *
     * @return group count
     */
    @Override
    public int getGroupCount() {
        return this.reportListDetail.size();
    }

    /**
     *
     * @param groupPosition
     * @return group Position as ID
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     *
     * @param groupPosition
     * @param isExpanded
     * @param convertView
     * @param parent
     * @return group View
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.history_list_group, null);
            String appName = "";
            PackageManager packageManager = context.getPackageManager();

            try{
                appName = reportListDetail.get(uidList.get(groupPosition)).get(0).getAppName();
            } catch(IndexOutOfBoundsException e){
                if(Collector.getKnownUIDs().containsKey(uidList.get(groupPosition))){
                    appName = Collector.getKnownUIDs().get(uidList.get(groupPosition));
                } else {
                    appName = packageManager.getNameForUid((new Integer(uidList.get(groupPosition))));
                }
            }

            TextView historyGroupTitle = (TextView) convertView.findViewById(R.id.historyGroupTitle);
            TextView historyGroupSubtitle = (TextView) convertView.findViewById(R.id.historyGroupSubtitle);

            try {
                historyGroupTitle.setText((String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(appName, PackageManager.GET_META_DATA)));
            } catch (PackageManager.NameNotFoundException e) {
                historyGroupTitle.setText(appName);
            }

            historyGroupSubtitle.setText(appName);

            ImageView imgView = (ImageView) convertView.findViewById(R.id.historyGroupIcon);

            try {
                imgView.setImageDrawable(packageManager.getApplicationIcon(appName));
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
