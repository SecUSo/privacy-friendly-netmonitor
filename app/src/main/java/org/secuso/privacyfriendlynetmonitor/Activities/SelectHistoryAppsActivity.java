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

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.DBApp;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.DaoSession;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntity;
import org.secuso.privacyfriendlynetmonitor.DatabaseUtil.ReportEntityDao;
import org.secuso.privacyfriendlynetmonitor.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Activity for the list of all installed apps with internet permission.
 * The list can be sorted alphabeic and after installed date.
 * A search function for the list is implemented.
 */

public class SelectHistoryAppsActivity extends AppCompatActivity {

    private ListView userInstalledAppsView;
    private List<String> app_list_name;
    private ReportEntityDao reportEntityDao;
    private List<String> appsToDelete;
    private AppListAdapter appAdapter;
    private SharedPreferences selectedAppsPreferences;
    private SharedPreferences.Editor editor;

    //Variables to sort alphabetic and accodring to installed date
    //the displayed app names are in the keys of the map, the value is the long Name
    private Map<String, String> sortMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_history_apps);

        // load DB
        DaoSession daoSession = ((DBApp) getApplication()).getDaoSession();
        reportEntityDao = daoSession.getReportEntityDao();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true);
        }

        selectedAppsPreferences = getSharedPreferences("SELECTEDAPPS", 0);
        editor = selectedAppsPreferences.edit();

        show_APP_list();
    }

    //method to load all the Apps in the listeview, sorted alphabetic form the start
    private void show_APP_list() {
        userInstalledAppsView = (ListView) findViewById(R.id.list_selection_app);
        app_list_name = provideAppList();
        appAdapter = new AppListAdapter(this, app_list_name);
        userInstalledAppsView.setAdapter(appAdapter);
    }

    //method to check for internet permission
    // 1. returns a list with Strings --> goes into the app_list_name
    // 2. fills the Map sort_map
    private List<String> provideAppList() {

        ArrayList<String> packageNames = new ArrayList<>();
        PackageManager p = this.getPackageManager();
        final List<PackageInfo> packs = p.getInstalledPackages(0);
        List<PackageInfo> packs_permission = p.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        //This FOR goes through every app that is installed on the device according to
        // --> p.getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo pinfo = packs.get(i); //This Var has the actual Information about an app (not the permission)
            PackageInfo appPermission = packs_permission.get(i);
            //If the App has NULL permissions then skip it
            if (appPermission.requestedPermissions == null) {
                continue;
            }
            //Check if App has Internet Permission
            for (String permission : appPermission.requestedPermissions) {
                //Checking for Internet permission
                if (TextUtils.equals(permission, android.Manifest.permission.INTERNET)) {
                    sortMap.put(pinfo.applicationInfo.loadLabel(getPackageManager()).toString(), pinfo.packageName);
                    break;
                }
            }
        }

        //Actual sorting alpabetic and convert into the String List "packageNames"
        Set set2 = sortMap.entrySet();
        Iterator iterator2 = set2.iterator();
        while (iterator2.hasNext()) {
            Map.Entry me2 = (Map.Entry) iterator2.next();
            packageNames.add(me2.getValue().toString());
        }

        return packageNames;
    }

    //enables the menue and provides the search method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.applistseletion_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        //search method
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String searchText) {
                app_list_name.clear();

                for (Map.Entry<String, String> entry : sortMap.entrySet()) {
                    if (entry.getKey().toLowerCase().contains(searchText.toLowerCase())) {
                        app_list_name.add(entry.getValue().toString());
                    }
                }

                userInstalledAppsView = (ListView) findViewById(R.id.list_selection_app);
                appAdapter = new AppListAdapter(SelectHistoryAppsActivity.this, app_list_name);
                userInstalledAppsView.setAdapter(appAdapter);

                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });

        MenuItem deleteItem = menu.findItem(R.id.deleteButton);
        deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                List<Integer> appsToDelete = appAdapter.getAppsToDelete();
                Toast toast;

                List<ReportEntity> reportEntities = reportEntityDao.loadAll();

                if (!appsToDelete.isEmpty()) {

                    for (Integer i : appsToDelete) {
                        String appName = (String) appAdapter.getItem(i);
                        for (ReportEntity reportEntity : reportEntities) {
                            if (reportEntity.getAppName().equals(appName)) {
                                reportEntityDao.delete(reportEntity);
                                editor.remove(appName);
                                editor.commit();
                            }
                        }
                        Collector.deleteAppFromIncludeInScan(appName);
                    }

                    toast = Toast.makeText(getApplicationContext(), "Reports have been deleted", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    toast = Toast.makeText(getApplicationContext(), "No reports available to delete.", Toast.LENGTH_SHORT);
                    toast.show();
                }

                return false;
            }
        });

        return true;
    }

    //inspection for sort selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // [See PFA Note APP]
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_alphabetical_asc) {
            item.setChecked(true);
            sortAlphabetic_asc();
        } else if (id == R.id.action_sort_alphabetical_desc) {
            item.setChecked(true);
            sortAlphabetic_desc();
        } else if (id == R.id.action_sort_installdate_asc) {
            item.setChecked(true);
            sortInstalledDate_asc();
        } else if (id == R.id.action_sort_installdate_desc) {
            item.setChecked(true);
            sortInstalledDate_desc();
        }
        userInstalledAppsView = (ListView) findViewById(R.id.list_selection_app);
        appAdapter = new AppListAdapter(this, app_list_name);
        userInstalledAppsView.setAdapter(appAdapter);

        return super.onOptionsItemSelected(item);
    }

    private void sortAlphabetic_asc() {
        app_list_name.clear();
        AscComparator comp = new AscComparator(sortMap);
        Map<String, String> newMap = new TreeMap(comp);
        newMap.putAll(sortMap);
        sortMap = newMap;

        for (Map.Entry<String, String> entry : sortMap.entrySet()) {
            app_list_name.add(entry.getValue().toString());
        }
    }

    private void sortAlphabetic_desc() {
        app_list_name.clear();
        DescComparator comp = new DescComparator(sortMap);
        Map<String, String> newMap = new TreeMap(comp);
        newMap.putAll(sortMap);
        sortMap = newMap;

        for (Map.Entry<String, String> entry : sortMap.entrySet()) {
            app_list_name.add(entry.getValue().toString());
        }
    }

    private void sortInstalledDate_asc() {
        Map<Long, List<String>> appdates = new TreeMap<>();
        PackageManager packageManager = this.getPackageManager();
        for (int i = 0; i < app_list_name.size(); i++) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = packageManager.getPackageInfo(app_list_name.get(i), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            long installTimeInMilliseconds = packageInfo.firstInstallTime;
            if (appdates.containsKey(installTimeInMilliseconds)) {
                appdates.get(installTimeInMilliseconds).add(app_list_name.get(i));
            } else {
                ArrayList<String> temp = new ArrayList<String>();
                temp.add(app_list_name.get(i));
                appdates.put(installTimeInMilliseconds, temp);
            }
        }
        AscComparator comp = new AscComparator(appdates);
        Map<Long, List<String>> newMap = new TreeMap(comp);
        newMap.putAll(appdates);
        appdates = newMap;
        app_list_name.clear();

        for (Long key : appdates.keySet()) {
            for(String s : appdates.get(key)){
                app_list_name.add(s);
            }
        }
    }

    private void sortInstalledDate_desc() {
        Map<Long, List<String>> appdates = new TreeMap<>();
        PackageManager packageManager = this.getPackageManager();
        for (int i = 0; i < app_list_name.size(); i++) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = packageManager.getPackageInfo(app_list_name.get(i), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            long installTimeInMilliseconds = packageInfo.firstInstallTime;
            if (appdates.containsKey(installTimeInMilliseconds)) {
                appdates.get(installTimeInMilliseconds).add(app_list_name.get(i));
            } else {
                ArrayList<String> temp = new ArrayList<String>();
                temp.add(app_list_name.get(i));
                appdates.put(installTimeInMilliseconds, temp);
            }
        }
        DescComparator comp = new DescComparator(appdates);
        Map<Long, List<String>> newMap = new TreeMap(comp);
        newMap.putAll(appdates);
        appdates = newMap;
        app_list_name.clear();

        for (Long key : appdates.keySet()) {
            for(String s : appdates.get(key)){
                app_list_name.add(s);
            }
        }
    }

    class AscComparator implements Comparator {
        Map map;

        public AscComparator(Map map) {
            this.map = map;
        }

        @Override
        public int compare(Object o1, Object o2) {
            return (o1.toString()).compareToIgnoreCase(o2.toString());
        }
    }

    class DescComparator implements Comparator {
        Map map;

        public DescComparator(Map map) {
            this.map = map;
        }

        @Override
        public int compare(Object o1, Object o2) {
            return (o2.toString()).compareToIgnoreCase(o1.toString());
        }
    }

    @Override
    public void onBackPressed() {
        if (appAdapter.getAppsToDelete() != null && !appAdapter.getAppsToDelete().isEmpty()) {
            List<Integer> appsToDelete = appAdapter.getAppsToDelete();
            for (int i : appsToDelete) {
                String appName = (String) appAdapter.getItem(i);
                ((RelativeLayout) userInstalledAppsView.getChildAt(i)).setBackgroundColor(Color.WHITE);
            }
            appAdapter.getAppsToDelete().clear();
        } else {
            super.onBackPressed();
        }
    }
}
