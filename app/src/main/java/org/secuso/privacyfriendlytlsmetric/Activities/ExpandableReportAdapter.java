package org.secuso.privacyfriendlytlsmetric.Activities;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlytlsmetric.R;

import java.text.CollationElementIterator;
import java.util.HashMap;
import java.util.List;


public class ExpandableReportAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Integer> uidList;
    private HashMap<Integer, List<Report>> reportListDetail;

    public ExpandableReportAdapter(Context context, List<Integer> expandableListTitle,
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
        if(r.isRemoteResolved()){
            text1 = "" + r.getRemoteAdd().getHostName();
        } else {
            text1 = "" + r.getRemoteAdd().getHostAddress();
        }
        //TODO: MS4/4 - add some warning if necessary
        final String text2 = "";

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

        if(Collector.mUidPackageMap.containsKey(uid)) {
            PackageInfo pi = Collector.mUidPackageMap.get(uid);
            textViewTitle.setText(pi.applicationInfo.name);
            textViewSubtitle.setText(pi.packageName);
            imgView.setImageDrawable(RunStore.getContext().getDrawable(pi.applicationInfo.icon));
        } else {
            textViewTitle.setText(R.string.unknown_app);
            textViewSubtitle.setText(R.string.unknown_package);
            imgView.setImageDrawable(RunStore.getContext().getDrawable(android.R.drawable.sym_def_app_icon));
        }
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

/* OLD Adapter below
    public ExpandableReportAdapter(Context context, Report[] reports) {
        super(context, R.layout.report_list_item, reports);
        this.context = context;
        this.reports = reports;
    }
}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.report_list_group, parent, false);

        //Ger the report
        Report r = reports[position];

        //First Line Text
        TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
        String first = r.getAppName();
        //String first = report.getAppName();
        firstLine.setText(first);

        //second Line Text
        TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
        String second = "Host: " + r.getRemoteAdd().getHostAddress() + ":" + r.getRemotePort()
                + "SrcPort:" + r.getLocalPort() + "Pid: " + r.getPid() + "Uid: " + r.getUid();
        secondLine.setText(second);

        //App icon
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        imageView.setImageResource(R.mipmap.icon);

        //Status icon
        //ImageView imageStatusView = (ImageView) rowView.findViewById(R.id.statusIcon);
        //imageStatusView.setImageResource(R.mipmap.icon_ok);


        //Status Text
        TextView statusLine = (TextView) rowView.findViewById(R.id.statusLine);
        String status = "Level :" + 0;
        statusLine.setText(status);

        return rowView;
    }*/
