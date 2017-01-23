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


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlynetmonitor.Assistant.Const;
import org.secuso.privacyfriendlynetmonitor.Assistant.PrefManager;
import org.secuso.privacyfriendlynetmonitor.Assistant.RunStore;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.PassiveService;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlynetmonitor.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.secuso.privacyfriendlynetmonitor.R.string.url;


/**
 * Activity providing main service controls and reports inspection
 */
public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout swipeRefreshLayout;
    private ExpandableListView expListView;
    private HashMap<Integer, List<Report>> reportMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunStore.setContext(this);
        RunStore.setAppContext(getApplicationContext());
        //Save context state
        if(!RunStore.getServiceHandler().isServiceRunning(PassiveService.class)){
            activateMainView();
        } else {
            activateReportView();
        }
        //Block Screenshot functionality
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        //Show welcome dialog on first start
//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean isFirstStart = sharedPrefs.getBoolean(Const.IS_FIRST_START, true);
//        if(isFirstStart){
//            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
//            startActivity(intent);
//            SharedPreferences.Editor edit = sharedPrefs.edit();
//            edit.putBoolean("IS_FIRST_START", false);
//            edit.apply();
//        }
        overridePendingTransition(0, 0);
    }

    // On start button press activate second view (report)
    private void setButtonListener() {
        final Button startStop = (Button) findViewById(R.id.main_button);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopTrigger();
            }
        });
    }

    //Trigger switches between activity, based service running indicator
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

    //Activate the main layout
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

    //activate the report layout
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

        //Showing Swipe Refresh animation on activity create
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
                expListView = (ExpandableListView) findViewById(R.id.list);
                ExpandableReportAdapter adapter = (ExpandableReportAdapter) expListView.getExpandableListAdapter();
                final Report r = (Report) adapter.getChild(i,i1);

                if(mSharedPreferences.getBoolean(Const.IS_DETAIL_MODE, false)) {
                    view.animate().setDuration(500).alpha((float) 0.5)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {

                                    Collector.provideDetail(r.uid, r.remoteAddHex);
                                    Intent intent = new Intent(getApplicationContext(), ReportDetailActivity.class);
                                    startActivity(intent);
                                }
                            });
                    return true;
                    // if no detail mode and server analysis is complete, goto SSL Labs
                } else if(mSharedPreferences.getBoolean(Const.IS_CERTVAL, false) &&
                        Collector.hasHostName(r.remoteAdd.getHostAddress()) &&
                        Collector.hasGrade(Collector.getDnsHostName(r.remoteAdd.getHostAddress()))) {
                    String url = Const.SSLLABS_URL +
                            Collector.getCertHost(Collector.getDnsHostName(r.remoteAdd.getHostAddress()));
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return false;

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

    //Set information to refresh view, when adapter is empty (no connections scanned yet)
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

    //Refresh the adapter when swipe triggers
    @Override
    public void onRefresh(){
        refreshAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    //listener of the toolbar buttons
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

    //refresh menu on layout change
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.clear();
        if(RunStore.getServiceHandler().isServiceRunning(PassiveService.class)) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }
}
