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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;

import org.secuso.privacyfriendlytlsmetric.Assistant.ContextStorage;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Evidence;
import org.secuso.privacyfriendlytlsmetric.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        HashMap<String, List<Report>> reportMap = Collector.provideReports();
        ArrayList<String> keyList = new ArrayList<String>(reportMap.keySet());

        final ExpandableListView expListView = (ExpandableListView) findViewById(R.id.reportExpandableListView);
        final ExpandableReportAdapter reportAdapter;
        reportAdapter = new ExpandableReportAdapter(this, keyList, reportMap);
        expListView.setAdapter(reportAdapter);

        //TODO: Change OnClickListener to PFA Design
/*        expListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        });*/
    }


    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_report;
    }


}