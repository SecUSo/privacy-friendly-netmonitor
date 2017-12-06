package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.Assistant.RunStore;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntity;
import org.secuso.privacyfriendlynetmonitor.R;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m4rc0 on 06.12.2017.
 */

public class HistoryDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        RunStore.setContext(this);

        //Get saved data from report entity
        List<String> details = getIntent().getStringArrayListExtra("Details");
        List<String[]> detailList = prepareData(details);


        final HistoryDetailActivity.DetailAdapter adapter = new HistoryDetailActivity.DetailAdapter(this, R.layout.report_detail_item, detailList);
        final ListView listview = (ListView) findViewById(R.id.report_detail_list_view);
        listview.setAdapter(adapter);

        View view_header = getLayoutInflater().inflate(R.layout.report_list_group_header, null);
        PackageManager packageManager = this.getPackageManager();
        String appName = details.get(0);

        ImageView imgView = (ImageView) view_header.findViewById(R.id.reportGroupIcon_header);
        TextView textView1 = (TextView) view_header.findViewById(R.id.reportGroupTitle_header);
        TextView textView2 = (TextView) view_header.findViewById(R.id.reportGroupSubtitle_header);

        try {
            imgView.setImageDrawable(packageManager.getApplicationIcon(appName));
            textView1.setText((String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(appName, PackageManager.GET_META_DATA)));
        } catch (PackageManager.NameNotFoundException e) {
        }

        textView2.setText(appName);

        listview.addHeaderView(view_header);
    }

    public List<String[]> prepareData(List<String> unpreparedDetails){


        List<String[]> detailsList = new ArrayList<String[]>();

        String[] details = new String[2];
        details[0] = "USER ID";
        details[1] = unpreparedDetails.get(1);
        detailsList.add(details);

        details = new String[2];
        details[0] = "APP VERSION";
        details[1] = unpreparedDetails.get(2);
        detailsList.add(details);

        details = new String[2];
        details[0] = "INSTALLED ON";
        details[1] = unpreparedDetails.get(3);
        detailsList.add(details);

        details = new String[2];
        details[0] = "REMOTE ADDRESS";
        details[1] = unpreparedDetails.get(4);
        detailsList.add(details);

        details = new String[2];
        details[0] = "REMOTE HEX";
        details[1] = unpreparedDetails.get(5);
        detailsList.add(details);

        details = new String[2];
        details[0] = "REMOTE HOST";
        details[1] = unpreparedDetails.get(6);
        detailsList.add(details);

        details = new String[2];
        details[0] = "LOCAL ADDRESS";
        details[1] = unpreparedDetails.get(7);
        detailsList.add(details);

        details = new String[2];
        details[0] = "LOCAL HEX";
        details[1] = unpreparedDetails.get(8);
        detailsList.add(details);

        details = new String[2];
        details[0] = "SERVICE PORT";
        details[1] = unpreparedDetails.get(9);
        detailsList.add(details);

        details = new String[2];
        details[0] = "PAYLOAD PROTOCOL";
        details[1] = unpreparedDetails.get(10);
        detailsList.add(details);

        details = new String[2];
        details[0] = "TRANSPORT PROTOCOL";
        details[1] = unpreparedDetails.get(11);
        detailsList.add(details);

        details = new String[2];
        details[0] = "LAST SEEN";
        details[1] = unpreparedDetails.get(12);
        detailsList.add(details);

        System.out.println(detailsList);

        return detailsList;
    }


    public class DetailAdapter extends ArrayAdapter<String[]> {

        DetailAdapter(Context context, int resource, List<String[]> detailList) {
            super(context, resource, detailList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.report_detail_item, null);
            }


            //Get string array and set it to text fields

            String[] detail = getItem(position);

            TextView type = (TextView) v.findViewById(R.id.report_detail_item_type);
            TextView value = (TextView) v.findViewById(R.id.report_detail_item_value);

            if (detail[0] != null && detail[1] != null) {
                type.setText(detail[0]);
                value.setText(detail[1]);
            } else {
                type.setText("");
                value.setText("");
            }
            return v;
        }
    }
}
