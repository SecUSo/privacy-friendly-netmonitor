package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.DBApp;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.DaoSession;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntity;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntityDao;
import org.secuso.privacyfriendlynetmonitor.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HistoryActivity extends BaseActivity {

    private ExpandableListView expListView;
    private ExpandableHistoryListAdapter historyReportAdapter;

    private static ReportEntityDao reportEntityDao;
    private HashMap<String, List<ReportEntity>> historyReportMap;
    private List<String> keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HistoryActivity.this, SelectHistoryAppsActivity.class));
            }
        });

        // load DB
        DaoSession daoSession = ((DBApp) getApplication()).getDaoSession();
        reportEntityDao = daoSession.getReportEntityDao();

        activateHistoryView();
    }

    private void activateHistoryView(){
        expListView = (ExpandableListView) findViewById(R.id.list_history);
        final HashMap<String, List<ReportEntity>> historyReports = provideHistoryReports();

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
    }

    /**
     * Get details from db entities an save in List
     * @param reportEntity
     * @return details list
     */
    private List<String> prepareData(ReportEntity reportEntity){
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
     *
     * @return HashMap with saved Reports
     */
    private HashMap<String, List<ReportEntity>> provideHistoryReports(){

        historyReportMap = new HashMap<String, List<ReportEntity>>();

        List<String> userIDs = new ArrayList<String>();
        List<ReportEntity> allReportEntities = reportEntityDao.loadAll();
        List<List<ReportEntity>> sortedAllReportEntities = new ArrayList<List<ReportEntity>>();

        for(ReportEntity reportEntity : allReportEntities){
            String userID = reportEntity.getUserID();
            if(!userIDs.contains(userID)){
                userIDs.add(userID);
                List<ReportEntity> tempReportList = new ArrayList<ReportEntity>();
                tempReportList.add(reportEntity);
                historyReportMap.put(userID, tempReportList);
            } else {
                historyReportMap.get(userID).add(reportEntity);
            }
        }

        keys = new ArrayList<>(historyReportMap.keySet());

        for(String key : keys){
            Collections.reverse(historyReportMap.get(key));
        }

        return historyReportMap;
    }

    protected int getNavigationDrawerID() { return R.id.nav_history; }

}
