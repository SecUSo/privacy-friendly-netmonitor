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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlytlsmetric.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Lists a Report of each detected connection. Most critical if several reports exist.
 */
public class ReportActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    private ExpandableListView expListView;
    private HashMap<Integer, List<Report>> reportMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        reportMap = Collector.provideSimpleReports();
        expListView = (ExpandableListView) findViewById(R.id.list);
        final ExpandableReportAdapter reportAdapter = new ExpandableReportAdapter(this, new ArrayList<>(reportMap.keySet()), reportMap);
        expListView.setAdapter(reportAdapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        refreshAdapter();
                                    }
                                }
        );

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                if(sharedPref.getBoolean("pref_advanced_switch", true)) {
                    view.animate().setDuration(500).alpha((float) 0.5)
                    .withEndAction(new Runnable() {

                        @Override
                        public void run() {
                            //TODO: Get Report, start smth
                            //Intent intent = new Intent(RunStore.getContext(), EvidenceDetailActivity.class);
                            //startActivity(intent);
                        }
                    });
                    return true;
                } else {
                    return false;
                }
            }


        });
    }





    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_report;
    }

    //refresh the adapter-list
    public void refreshAdapter(){
        swipeRefreshLayout.setRefreshing(true);

        reportMap = Collector.provideSimpleReports();
        final ExpandableReportAdapter reportAdapter = new ExpandableReportAdapter(this, new ArrayList<>(reportMap.keySet()), reportMap);
        expListView.setAdapter(reportAdapter);

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh(){
        Log.e(Const.LOG_TAG, "onRefresh: triggered");
        refreshAdapter();
    }
}