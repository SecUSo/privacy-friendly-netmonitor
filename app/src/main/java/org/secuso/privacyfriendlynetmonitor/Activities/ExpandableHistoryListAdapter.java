package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.Assistant.KnownPorts;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntity;
import org.secuso.privacyfriendlynetmonitor.R;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by m4rc0 on 04.12.2017.
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
        history_item_2_val.setText(reportEntity.getLastSeen());


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

            String appName = reportListDetail.get(uidList.get(groupPosition)).get(0).getAppName();

            TextView historyGroupTitle = (TextView) convertView.findViewById(R.id.historyGroupTitle);
            TextView historyGroupSubtitle = (TextView) convertView.findViewById(R.id.historyGroupSubtitle);

            PackageManager packageManager = context.getPackageManager();

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
