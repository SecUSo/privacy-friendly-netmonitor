package org.secuso.privacyfriendlytlsmetric.Activities;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlytlsmetric.Assistant.KnownPorts;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlytlsmetric.R;

import java.util.HashMap;
import java.util.List;


public class ExpandableReportAdapter extends BaseExpandableListAdapter {

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
        final String text1;
        final String text2;
        if(r.remoteResolved){
            text1 = "" + r.remoteAdd.getHostName();
        } else {
            text1 = "" + r.remoteAdd.getHostAddress();
        }
        if (r.remotePort == 443 && r.remoteResolved){
            text2 = "Host TLS rating: " + Collector.getMetric(text1);
        } else {
            text2 = "protocol: " + KnownPorts.resolvePort(r.remotePort) + " (" + r.type + ")";
        }

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.report_list_item, null);
        }
        TextView reportTextView = (TextView) convertView
                .findViewById(R.id.report_item_1);
        reportTextView.setText(text1);
        reportTextView = (TextView) convertView
                .findViewById(R.id.report_item_2);
        reportTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        reportTextView.setText(text2);
        reportTextView.setTextColor(context.getResources().getColor(R.color.middlegrey));
        return convertView;
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

        textViewTitle.setText(Collector.getLabel(uid) +
                " (" + reportListDetail.get(uid).size() +")");
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