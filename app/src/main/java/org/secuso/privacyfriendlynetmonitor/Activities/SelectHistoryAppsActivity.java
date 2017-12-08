package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.graphics.drawable.Drawable;

import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntity;
import org.secuso.privacyfriendlynetmonitor.R;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.xml.transform.sax.SAXSource;

public class SelectHistoryAppsActivity extends BaseActivity {

    private ListView userInstalledAppsView;
    private List<App_Entity> app_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_history_apps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        show_APP_list();
    }

    private void show_APP_list(){
        userInstalledAppsView = (ListView) findViewById(R.id.list_selection_app_history);
        List<App_Entity> app_list = provideAppList();
        userInstalledAppsView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        AppListAdapter appAdapter = new AppListAdapter(SelectHistoryAppsActivity.this, app_list);
        userInstalledAppsView.setAdapter(appAdapter);
    }

    private List<App_Entity> provideAppList() {
        app_list = new ArrayList<App_Entity>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for (int i =0; i<packs.size();i++){
            PackageInfo p = packs.get(i);
            if ((isSystemPackage(p) == false)) {
                String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
                Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
                app_list.add(new App_Entity(appName, icon));
            }
        }

        return app_list;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
    }


    protected int getNavigationDrawerID() { return R.id.nav_history; }

}
