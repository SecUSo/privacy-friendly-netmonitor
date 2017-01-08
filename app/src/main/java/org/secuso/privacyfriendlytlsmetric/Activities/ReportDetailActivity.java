/*
    TLSMetric
    - Copyright (2015, 2016) Felix Tsala Schiller

    ###################################################################

    This file is part of TLSMetric.

    TLSMetric is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TLSMetric is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TLSMetric.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von TLSMetric.

    TLSMetric ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    TLSMetric wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlytlsmetric.Activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlytlsmetric.R;

import de.bjoernr.ssllabs.ConsoleUtilities;

import static org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector.getDnsHostName;
import static org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector.isCertVal;
import static org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector.mCertValMap;

/**
 * Evidence Detail Panel. List all reports of a connection, invoked by Evidence Panel (ReportActivity)
 */
public class ReportDetailActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        RunStore.setContext(this);

        ArrayList<String[]> detailList = Collector.sDetailReportList;
        final DetailAdapter adapter = new DetailAdapter(this, R.layout.report_detail_item, detailList);
        final ListView listview = (ListView) findViewById(R.id.report_detail_list_view);
        listview.setAdapter(adapter);

        // Fill headings
        Report r = Collector.sDetailReport;
        ImageView icon = (ImageView) findViewById(R.id.reportDetailIcon);
        icon.setImageDrawable(Collector.getIcon(r.uid));
        TextView label = (TextView) findViewById(R.id.reportDetailTitle);
        label.setText(Collector.getLabel(r.uid));
        TextView pkg = (TextView) findViewById(R.id.reportDetailSubtitle);
        pkg.setText(Collector.getPackage(r.uid));
        //Add certificate information
        if(isCertVal && Collector.hasHostName(r.remoteAdd.getHostAddress()) &&
                mCertValMap.containsKey(getDnsHostName(r.remoteAdd.getHostAddress()))){
            TextView ssllabs = (TextView) findViewById(R.id.report_detail_ssllabs_heading);
            ssllabs.setText(getResources().getString(R.string.report_detail_ssllabs));
            ssllabs = (TextView) findViewById(R.id.report_detail_ssllabs_result);
            ssllabs.setText(ConsoleUtilities.mapToConsoleOutput(
                    mCertValMap.get(getDnsHostName(r.remoteAdd.getHostAddress()))));
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //Implementation of List Adapter
    class DetailAdapter extends ArrayAdapter<String[]> {

        public DetailAdapter(Context context, int resource) {

            super(context, resource);
        }

        public DetailAdapter(Context context, int resource, List<String[]> detailList) {

            super(context, resource, detailList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.report_detail_item, null);
            }

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
        return R.id.nav_main;
    }

}

