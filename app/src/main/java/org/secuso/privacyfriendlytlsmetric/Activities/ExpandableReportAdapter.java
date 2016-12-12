package org.secuso.privacyfriendlytlsmetric.Activities;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlytlsmetric.R;

import java.util.HashMap;
import java.util.List;


public class ExpandableReportAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> reportListTitle;
    private HashMap<String, List<String>> reportListDetail;

    public ExpandableReportAdapter(Context context, List<String> expandableListTitle,
                                   HashMap<String, List<String>> expandableListDetail) {
        this.context = context;
        this.reportListTitle = expandableListTitle;
        this.reportListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.reportListDetail.get(this.reportListTitle.get(listPosition))
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

        final String text1 = (String) getChild(listPosition, expandedListPosition);
        //final String text2 = "" + r.getLocalAdd() + r.getRemotePort() + " -> " + r.getRemoteAdd() + r.getRemotePort();
        //final String text3 = "UID: " + r.getUid() + "PID: " + r.getPid();

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.report_list_item, null);
        }
        TextView reportTextView = (TextView) convertView
                .findViewById(R.id.report_item_1);
        reportTextView.setText(text1);
        /*reportTextView = (TextView) convertView
                .findViewById(R.id.report_item_2);
        reportTextView.setText(text2);
        reportTextView = (TextView) convertView
                .findViewById(R.id.report_item_3);
        reportTextView.setText(text3);
        */
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.reportListDetail.get(this.reportListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.reportListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.reportListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.report_list_group, null);
        }
        TextView textView = (TextView) convertView
                .findViewById(R.id.reportGroupTitle);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setText(listTitle);
        textView = (TextView) convertView
                .findViewById(R.id.reportGroupSubtitle);
        //TODO: get full qualified package name??
        textView.setText(listTitle);
        ImageView imgView = (ImageView) convertView.findViewById(R.id.reportGroupIcon);
        imgView.setImageDrawable(Collector.mPackageMap.get(listTitle).icon);
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
        String first = r.getPackageName();
        //String first = report.getPackageName();
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
