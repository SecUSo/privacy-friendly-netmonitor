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
        final String item1;
        final String item2_type;
        final String item2_value;
        final String item3;

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
        final int height = textView.getHeight();
        textView.setText(item1);
        textView = (TextView) convertView.findViewById(R.id.report_item_2_type);
        textView.setText(item2_type);
        textView = (TextView) convertView.findViewById(R.id.report_item_2_val);
        textView.setText(item2_value);

        //Set warning colour
        if (item2_value.contains(Const.STATUS_TLS)) {
            textView.setTextColor(context.getResources().getColor(R.color.green));
        } else if (item2_value.contains(Const.STATUS_UNSECURE)){
            textView.setTextColor(context.getResources().getColor(R.color.red));
        }
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