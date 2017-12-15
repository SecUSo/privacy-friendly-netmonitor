package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
    private List<String> app_list_name;

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
        userInstalledAppsView = (ListView) findViewById(R.id.list_selection_app);
        app_list_name = provideAppList();
        //userInstalledAppsView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        AppListAdapter appAdapter = new AppListAdapter(this, app_list_name);
        userInstalledAppsView.setAdapter(appAdapter);
    }

    private List<String> provideAppList() {

        ArrayList<String> packageNames = new ArrayList<String>();
        PackageManager p = this.getPackageManager();
        final List<PackageInfo> packs = p.getInstalledPackages(0);
        List<PackageInfo> packs_permission = p.getInstalledPackages(PackageManager.GET_PERMISSIONS);


        //This For goes through every app that is installed on the device according to
        // --> p.getInstalledPackages(0);
        for (int i =0; i<packs.size();i++){
            PackageInfo pinfo = packs.get(i); //This Var has the actual Information about an app (not the permission)
            //Check if it is a System App
            if ((isSystemPackage(pinfo) == false)) {
                PackageInfo appPermission = packs_permission.get(i);
                //If the App has NULL permissions then skip it
                if (appPermission.requestedPermissions == null){
                    continue;
                }

                //Check if App has Internet Permission
                for (String permission : appPermission.requestedPermissions) {
                    //Checking for Internet permission
                    if (TextUtils.equals(permission, android.Manifest.permission.INTERNET)) {
                        //Actual Data collection
                        packageNames.add(pinfo.packageName);
                        String appName = pinfo.applicationInfo.loadLabel(getPackageManager()).toString();
                        Drawable icon = pinfo.applicationInfo.loadIcon(getPackageManager());
                        break;
                    }
                }

            }
        }

        return packageNames;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
    }

    protected int getNavigationDrawerID() { return R.id.nav_history; }

}
