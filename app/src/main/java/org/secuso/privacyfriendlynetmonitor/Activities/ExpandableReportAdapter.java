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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.Assistant.Const;
import org.secuso.privacyfriendlynetmonitor.Assistant.KnownPorts;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlynetmonitor.R;

import java.util.HashMap;
import java.util.List;

/**
 * Adapter displaying information in the ReportActivity
 */
class ExpandableReportAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Integer> uidList;
    private HashMap<Integer, List<Report>> reportListDetail;

    ExpandableReportAdapter(Context context, List<Integer> expandableListTitle,
                            HashMap<Integer, List<Report>> expandableListDetail) {
        this.context = context;
        this.uidList = expandableListTitle;
        this.reportListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.reportListDetail.get(this.uidList.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {

        //Build information from reports of one App (UID)
        Report r = (Report) getChild(listPosition, expandedListPosition);
        final String item1;
        final String item2_type;
        final String item2_value;

        //Set hostname if resolved by AsyncDNS class
        if(Collector.hasHostName(r.remoteAdd.getHostAddress())){
            item1 = Collector.getDnsHostName(r.remoteAdd.getHostAddress());
        } else {
            item1 = "" + r.remoteAdd.getHostAddress();
        }
        //Set connection info or server rating
        if (Collector.isCertVal && KnownPorts.isTlsPort(r.remotePort) && Collector.hasHostName(r.remoteAdd.getHostAddress())){
            if(item1.equals(Collector.getCertHost(item1))) {
                item2_type = "SSL Server Rating:";
                item2_value = Collector.getMetric(item1);
            } else {
                item2_type = "SSL Server Rating:";
                if (item1.equals(Collector.getCertHost(item1))) {
                    item2_value = Collector.getMetric(item1);
                } else {
                    item2_value = Collector.getMetric(item1) + " (" + Collector.getCertHost(item1)
                            + ")";
                }
            }
        } else {
            item2_type = "Connection Info:";
            item2_value = KnownPorts.CompileConnectionInfo(r.remotePort, r.type);
        }

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.report_list_item, null);
        }

        //Fill textviews
        TextView textView = (TextView) convertView.findViewById(R.id.report_item_1);
        //final int height = textView.getHeight();
        textView.setText(item1);
        textView = (TextView) convertView.findViewById(R.id.report_item_2_type);
        textView.setText(item2_type);
        textView = (TextView) convertView.findViewById(R.id.report_item_2_val);
        textView.setText(item2_value);

        //Set warning colour
        textView.setTextColor(context.getResources().getColor(getWarningColor(item2_value)));


        return convertView;
    }

    private int getWarningColor(String value) {
        if (value.contains(Const.STATUS_TLS) || value.substring(0,1).equals("A")) {
            return (R.color.green);
        } else if (value.substring(0,1).equals("B") || value.substring(0,1).equals("C")){
            return (R.color.orange);
        } else if (value.contains(Const.STATUS_UNSECURE) || value.substring(0,1).equals("T") ||
                value.substring(0,1).equals("F") || value.substring(0,1).equals("D") ||
                value.substring(0,1).equals("E")){
            return R.color.red;
        } else {
            return R.color.text_dark;
        }
    }


    @Override
    public int getChildrenCount(int listPosition) {
        return this.reportListDetail.get(this.uidList.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.uidList.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.uidList.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        int uid = (int) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.report_list_group, null);
        }
        TextView textViewTitle = (TextView) convertView.findViewById(R.id.reportGroupTitle);
        textViewTitle.setTypeface(null, Typeface.BOLD);
        TextView textViewSubtitle = (TextView) convertView.findViewById(R.id.reportGroupSubtitle);
        ImageView imgView = (ImageView) convertView.findViewById(R.id.reportGroupIcon);

        //add system app tag
        if(uid <= 10000){
            textViewTitle.setText(Collector.getLabel(uid) +
                    " (" + reportListDetail.get(uid).size() + ")" + " [System]");
        } else {
            textViewTitle.setText(Collector.getLabel(uid) +
                    " (" + reportListDetail.get(uid).size() + ")");
        }
        textViewSubtitle.setText(Collector.getPackage(uid));
        imgView.setImageDrawable(Collector.getIcon(uid));

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

}