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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.secuso.privacyfriendlynetmonitor.Assistant.Const;
import org.secuso.privacyfriendlynetmonitor.Assistant.RunStore;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlynetmonitor.R;

/**
 * Evidence Detail Panel. List all reports of a connection, invoked by Evidence Panel (ReportActivity)
 */
public class ReportDetailActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        RunStore.setContext(this);

        //block screenshot functionality
        //Block Screenshot functionality
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        //Get reports from collector class
        ArrayList<String[]> detailList = Collector.sDetailReportList;
        final DetailAdapter adapter = new DetailAdapter(this, R.layout.report_detail_item, detailList);
        final ListView listview = (ListView) findViewById(R.id.report_detail_list_view);
        listview.setAdapter(adapter);

        // Fill headings
        final Report r = Collector.sDetailReport;
        ImageView icon = (ImageView) findViewById(R.id.reportDetailIcon);
        icon.setImageDrawable(Collector.getIcon(r.uid));
        TextView label = (TextView) findViewById(R.id.reportDetailTitle);
        label.setText(Collector.getLabel(r.uid));
        TextView pkg = (TextView) findViewById(R.id.reportDetailSubtitle);
        pkg.setText(Collector.getPackage(r.uid));

        //Add certificate information - open link to ssl labs
        if(mSharedPreferences.getBoolean(Const.IS_CERTVAL, false) && Collector.hasHostName(r.remoteAdd.getHostAddress()) &&
                Collector.hasGrade(Collector.getDnsHostName(r.remoteAdd.getHostAddress()))){
            TextView ssllabs = (TextView) findViewById(R.id.report_detail_ssllabs_result);
            ssllabs.setVisibility(View.VISIBLE);
            ssllabs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = Const.SSLLABS_URL +
                            Collector.getCertHost(Collector.getDnsHostName(r.remoteAdd.getHostAddress()));
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            });

        }


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //Implementation of List Adapter
    class DetailAdapter extends ArrayAdapter<String[]> {

        DetailAdapter(Context context, int resource, List<String[]> detailList) {

            super(context, resource, detailList);
        }

        //Get detail information from collector class and write to adapter views
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

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

    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }

}

