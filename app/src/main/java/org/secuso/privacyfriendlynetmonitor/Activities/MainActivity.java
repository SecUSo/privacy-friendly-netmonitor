package org.secuso.privacyfriendlynetmonitor.Activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.Assistant.Const;
import org.secuso.privacyfriendlynetmonitor.Assistant.RunStore;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.PassiveService;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlynetmonitor.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout swipeRefreshLayout;
    private ExpandableListView expListView;
    private HashMap<Integer, List<Report>> reportMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunStore.setContext(this);
        //Save context state
        if(!RunStore.getServiceHandler().isServiceRunning(PassiveService.class)){
            activateMainView();
        } else {
            activateReportView();
        }
        //Show welcome dialog on first start
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstStart = sharedPrefs.getBoolean(Const.IS_FIRST_START, true);
        if(isFirstStart){
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
            SharedPreferences.Editor edit = sharedPrefs.edit();
            edit.putBoolean("IS_FIRST_START", false);
            edit.apply();
        }
        overridePendingTransition(0, 0);
    }

    private void setButtonListener() {
        final Button startStop = (Button) findViewById(R.id.main_button);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopTrigger();
            }
        });
    }

    private void startStopTrigger() {
        if (!RunStore.getServiceHandler().isServiceRunning(PassiveService.class)) {
            if (Const.IS_DEBUG)
                Log.d(Const.LOG_TAG, getResources().getString(R.string.passive_service_start));
            RunStore.getServiceHandler().startPassiveService();
            Intent intent = new Intent(RunStore.getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            if (Const.IS_DEBUG)
                Log.d(Const.LOG_TAG, getResources().getString(R.string.passive_service_stop));
            RunStore.getServiceHandler().stopPassiveService();
            Intent intent = new Intent(RunStore.getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private void activateMainView() {
        setContentView(R.layout.activity_main);
        super.setToolbar();

        final Button startStop = (Button) findViewById(R.id.main_button);
        startStop.setText(R.string.main_button_text_off);
        TextView textView = (TextView) findViewById(R.id.main_text_startstop);
        textView.setText(R.string.main_text_stopped);
        setButtonListener();
        getNavigationDrawerID();
    }

    private void activateReportView(){
        setContentView(R.layout.activity_report);
        super.setToolbar();
        getNavigationDrawerID();

        //Initiate ListView functionality
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
            public boolean onChildClick(ExpandableListView expandableListView, View view, final int i, final int i1, final long l) {
                if(mSharedPreferences.getBoolean(Const.DETAIL_MODE, true)) {
                    view.animate().setDuration(500).alpha((float) 0.5)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    expListView = (ExpandableListView) findViewById(R.id.list);
                                    ExpandableReportAdapter adapter = (ExpandableReportAdapter) expListView.getExpandableListAdapter();
                                    Report r = (Report) adapter.getChild(i,i1);
                                    Collector.provideDetail(r.uid, r.remoteAddHex);
                                    Intent intent = new Intent(getApplicationContext(), ReportDetailActivity.class);
                                    startActivity(intent);
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
        return R.id.nav_main;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //refresh the adapter-list
    public void refreshAdapter(){
        swipeRefreshLayout.setRefreshing(true);

        reportMap = Collector.provideSimpleReports();
        final ExpandableReportAdapter reportAdapter = new ExpandableReportAdapter(this, new ArrayList<>(reportMap.keySet()), reportMap);
        expListView.setAdapter(reportAdapter);

        //Set swipe text and icon visible, if connections are empty
        setSwipeInfo((reportAdapter.getGroupCount() > 0));
        swipeRefreshLayout.setRefreshing(false);
    }

    private void setSwipeInfo(boolean b) {
        final ImageView icon = (ImageView) findViewById(R.id.report_empty_icon);
        final TextView text = (TextView) findViewById(R.id.report_empty_text);
        if(b){
            icon.setVisibility(View.GONE);
            text.setVisibility(View.GONE);
        } else {
            icon.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh(){
        refreshAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_refresh:
                refreshAdapter();
                break;
            case R.id.action_startstop:
                startStopTrigger();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.clear();
        if(RunStore.getServiceHandler().isServiceRunning(PassiveService.class)) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }
}
