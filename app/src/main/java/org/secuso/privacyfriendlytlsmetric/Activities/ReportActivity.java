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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlytlsmetric.Assistant.ContextStorage;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Detector;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Evidence;
import org.secuso.privacyfriendlytlsmetric.R;

/**
 * Lists a Report of each detected connection. Most critical if several reports exist.
 */
public class ReportActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        ContextStorage.setContext(this);
        Evidence.newWarnings = 0;

        /*
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.evidence_toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setLogo(R.mipmap.icon);
        toolbar.setLogoDescription(R.string.app_name);
        */

        //EvidenceList
        final ListView listview = (ListView) findViewById(android.R.id.list);
        final EvidenceAdapter adapter;
        adapter = new EvidenceAdapter(this, Collector.mReportArray);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final Report report = (Report) parent.getItemAtPosition(position);
                view.animate().setDuration(500).alpha((float)0.5)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                    Evidence.setSortedEvidenceDetail(report.getLocalPort());
                                    Intent intent = new Intent(ContextStorage.getContext(), EvidenceDetailActivity.class);
                                    startActivity(intent);
                            }
                        });
            }

        });
    }

 /*   //old menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_back:
                Intent intent = new Intent(ContextStorage.getContext(), MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_refresh:
                int level = Detector.getmUpdateType();
                Detector.setmUpdateType(0);
                Detector.updateReportMap();
                Detector.setmUpdateType(level);
                final ListView listView = (ListView) findViewById(android.R.id.list);
                final EvidenceAdapter adapter = new EvidenceAdapter(this, Collector.mReportArray);
                listView.setAdapter(adapter);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_report;
    }

    //Customized Adapter class for display of Evidence Reports
    private class EvidenceAdapter extends ArrayAdapter<Report> {

        private final Context context;
        private Report[] reports;

        public EvidenceAdapter(Context context, Report[] reports) {
            super(context, R.layout.evidence_list_entry, reports);
            this.context = context;
            this.reports = reports;
            }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
         LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.evidence_list_entry, parent, false);

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
        }
    }


}