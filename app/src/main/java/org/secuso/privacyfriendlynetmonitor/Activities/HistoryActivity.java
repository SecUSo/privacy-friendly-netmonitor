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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.DBApp;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.DaoSession;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntity;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntityDao;
import org.secuso.privacyfriendlynetmonitor.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Acitivity for handling the history. All the selected Apps are tracked and shown as a history
 */

public class HistoryActivity extends BaseActivity {

    private ExpandableListView expListView;
    private ExpandableHistoryListAdapter historyReportAdapter;

    private ReportEntityDao reportEntityDao;
    private HashMap<String, List<ReportEntity>> historyReportMap;
    private List<String> keys;

    private SharedPreferences selectedAppsPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // load DB
        DaoSession daoSession = ((DBApp) getApplication()).getDaoSession();
        reportEntityDao = daoSession.getReportEntityDao();

        selectedAppsPreferences = getSharedPreferences("SELECTEDAPPS", 0);
        editor = selectedAppsPreferences.edit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HistoryActivity.this, SelectHistoryAppsActivity.class));
            }
        });

        // delete DB
        Button deleteDB = (Button) findViewById(R.id.deleteDB);
        deleteDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportEntityDao.deleteAll();
                Collector.getAppsToIncludeInScan().clear();
                editor.clear();
                editor.commit();
                activateHistoryView();
            }
        });
        
        activateHistoryView();
    }

    private void activateHistoryView() {
        expListView = (ExpandableListView) findViewById(R.id.list_history);
        final HashMap<String, List<ReportEntity>> historyReports = provideHistoryReports();

        TextView textView = (TextView) findViewById(R.id.noData);
        if (historyReports.isEmpty()) {
            if (textView.getVisibility() == View.INVISIBLE) {
                textView.setVisibility(View.VISIBLE);
            }
        } else {

            if (textView.getVisibility() == View.VISIBLE) {
                textView.setVisibility(View.INVISIBLE);
            }
        }

        historyReportAdapter = new ExpandableHistoryListAdapter(this, new ArrayList<>(historyReports.keySet()), historyReports);

        expListView.setAdapter(historyReportAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ReportEntity reportEntity = historyReports.get(keys.get(groupPosition)).get(childPosition);
                List<String> detailsList = prepareData(reportEntity);
                Intent intent = new Intent(getBaseContext(), HistoryDetailActivity.class);
                intent.putExtra("Details", (ArrayList) detailsList);
                startActivity(intent);
                return false;
            }
        });

        expListView.setOnItemLongClickListener(new ExpandableListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tx = (TextView) view.findViewById(R.id.historyGroupTitle);
                String s = tx.getText().toString();

                Intent myIntent = new Intent(HistoryActivity.this, AppReport_Detail.class);
                myIntent.putExtra("AppName", s);
                startActivity(myIntent);

                return false;
            }
        });
    }

    /**
     * Get details from db entities an save in List
     *
     * @param reportEntity
     * @return details list
     */
    private List<String> prepareData(ReportEntity reportEntity) {
        String details = "";
        List<String> detailsList = new ArrayList<String>();

        details = reportEntity.getAppName();
        detailsList.add(details);

        details = reportEntity.getUserID();
        detailsList.add(details);

        details = reportEntity.getAppVersion();
        detailsList.add(details);

        details = reportEntity.getInstalledOn();
        detailsList.add(details);

        details = reportEntity.getRemoteAddress();
        detailsList.add(details);

        details = reportEntity.getRemoteHex();
        detailsList.add(details);

        details = reportEntity.getRemoteHost();
        detailsList.add(details);

        details = reportEntity.getLocalAddress();
        detailsList.add(details);

        details = reportEntity.getLocalHex();
        detailsList.add(details);

        details = reportEntity.getServicePoint();
        detailsList.add(details);

        details = reportEntity.getPayloadProtocol();
        detailsList.add(details);

        details = reportEntity.getTransportProtocol();
        detailsList.add(details);

        details = reportEntity.getLastSeen();
        detailsList.add(details);

        details = reportEntity.getLocalPort();
        detailsList.add(details);

        details = reportEntity.getLastSocketState();
        detailsList.add(details);

        details = reportEntity.getConnectionInfo();
        detailsList.add(details);

        return detailsList;
    }

    /**
     * @return HashMap with saved Reports
     */
    private HashMap<String, List<ReportEntity>> provideHistoryReports() {

        historyReportMap = new HashMap<String, List<ReportEntity>>();
        List<String> appendedApps = new ArrayList<String>();

        List<String> userIDs = new ArrayList<String>();
        List<ReportEntity> allReportEntities = reportEntityDao.loadAll();
        List<List<ReportEntity>> sortedAllReportEntities = new ArrayList<List<ReportEntity>>();

        for (ReportEntity reportEntity : allReportEntities) {
            String userID = reportEntity.getUserID();
            if (!userIDs.contains(userID)) {
                userIDs.add(userID);
                List<ReportEntity> tempReportList = new ArrayList<ReportEntity>();
                tempReportList.add(reportEntity);

                if (!appendedApps.contains(reportEntity.getAppName())) {
                    appendedApps.add(reportEntity.getAppName());
                }

                historyReportMap.put(userID, tempReportList);
            } else {
                historyReportMap.get(userID).add(reportEntity);
            }
        }

        keys = new ArrayList<>(historyReportMap.keySet());

        for (String key : keys) {
            Collections.reverse(historyReportMap.get(key));
        }

        List<String> appsToInclude = Collector.getAppsToIncludeInScan();
        if (!appsToInclude.isEmpty()) {
            appsToInclude = new ArrayList<String>(new LinkedHashSet<String>(appsToInclude));
            appsToInclude.removeAll(appendedApps);
            if (!appsToInclude.isEmpty()) {
                for (String appName : appsToInclude) {
                    try {
                        System.out.println(appName);
                        int uid = getPackageManager().getApplicationInfo(appName, 0).uid;
                        if (!Collector.getKnownUIDs().containsKey(uid)) {
                            Collector.addKnownUIDs((new String()).valueOf(uid), appName);
                        }
                        historyReportMap.put((new String()).valueOf(uid), new ArrayList<ReportEntity>());
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                }
                appsToInclude.clear();
            }
        }

        return historyReportMap;
    }

    @Override
    public void onResume() {
        super.onResume();
        activateHistoryView();
    }

    protected int getNavigationDrawerID() {
        return R.id.nav_history;
    }

}
