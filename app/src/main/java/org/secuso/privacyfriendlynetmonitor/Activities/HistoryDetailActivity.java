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
import org.secuso.privacyfriendlynetmonitor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m4rc0 on 06.12.2017.
 * This actitivty takes care of the details of on history segment.
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

    /**
     *
     * @param unpreparedDetails
     * @return
     */
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
        details[0] = "";
        details[1] = "";
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
        details[0] = "";
        details[1] = "";
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

        details = new String[2];
        details[0] = "";
        details[1] = "";
        detailsList.add(details);

        details = new String[2];
        details[0] = "LOCAL PORT";
        details[1] = unpreparedDetails.get(13);
        detailsList.add(details);

        details = new String[2];
        details[0] = "LAST SOCKET STATE";
        details[1] = unpreparedDetails.get(14);
        detailsList.add(details);

        details = new String[2];
        details[0] = "CONNECTION INFO\n(OUTDATED)";
        details[1] = unpreparedDetails.get(15);
        detailsList.add(details);

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
