package org.secuso.privacyfriendlytlsmetric.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.PassiveService;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Report;
import org.secuso.privacyfriendlytlsmetric.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences sharedPref;
    private ExpandableListView expListView;
    private HashMap<Integer, List<Report>> reportMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RunStore.setContext(this);

        if(!RunStore.getServiceHandler().isServiceRunning(PassiveService.class)){
            activateMainView();
        } else {
            activateReportView();
        }

        //Show welcome dialog on first start
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstStart = sharedPrefs.getBoolean("IsFirstStart", true);
        if(isFirstStart){
            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.show(getFragmentManager(), "WelcomeDialog");
            SharedPreferences.Editor edit = sharedPrefs.edit();
            edit.putBoolean("IsFirstStart", false);
            edit.apply();
        }

        overridePendingTransition(0, 0);
    }

    private void setButtonListener() {
        final Button startStop = (Button) findViewById(R.id.main_button);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!RunStore.getServiceHandler().isServiceRunning(PassiveService.class)) {
                    if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, getResources().getString(R.string.passive_service_start));
                    RunStore.getServiceHandler().startPassiveService();
                    Intent intent = new Intent(RunStore.getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Collector.isCertVal = mSharedPreferences.getBoolean(Const.IS_CERTVAL, false);
                    startActivity(intent);
                } else {
                    if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, getResources().getString(R.string.passive_service_stop));
                    RunStore.getServiceHandler().stopPassiveService();
                    Intent intent = new Intent(RunStore.getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
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


        final Button startStop = (Button) findViewById(R.id.main_button);
        startStop.setText(R.string.main_button_text_on);
        setButtonListener();
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

    public static class WelcomeDialog extends DialogFragment {

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater i = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(i.inflate(R.layout.welcome_dialog, null));
            builder.setIcon(R.mipmap.icon);
            builder.setTitle(getActivity().getString(R.string.welcome));
            builder.setPositiveButton(getActivity().getString(R.string.okay), null);
            builder.setNegativeButton(getActivity().getString(R.string.viewhelp), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((MainActivity)getActivity()).goToNavigationItem(R.id.nav_help);
                }
            });

            return builder.create();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //Kill the service if main activity gets destroyed
        /*
        if(RunStore.getServiceHandler().isServiceRunning(PassiveService.class)) {
            RunStore.getServiceHandler().stopPassiveService();
        }*/
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
        refreshAdapter();
    }
}
