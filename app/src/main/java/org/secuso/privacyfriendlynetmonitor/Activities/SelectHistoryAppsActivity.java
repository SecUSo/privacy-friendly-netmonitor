package org.secuso.privacyfriendlynetmonitor.Activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.secuso.privacyfriendlynetmonitor.R;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class SelectHistoryAppsActivity extends AppCompatActivity{

    private ListView userInstalledAppsView;
    private List<String> app_list_name;

    //Variables to sort alphabetic
    //the displayed app names are in the keys of the map, the value is the long Name
    private Map<String, String> sortMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_history_apps);

        ActionBar ab =  getSupportActionBar();
        if(ab!=null){
            ab.setDisplayShowHomeEnabled(true);
        }
        show_APP_list();
    }

    private void show_APP_list(){
        userInstalledAppsView = (ListView) findViewById(R.id.list_selection_app);
        app_list_name = provideAppList();
        AppListAdapter appAdapter = new AppListAdapter(this, app_list_name);
        userInstalledAppsView.setAdapter(appAdapter);
    }

    private List<String> provideAppList() {

        ArrayList<String> packageNames = new ArrayList<String>();
        PackageManager p = this.getPackageManager();
        final List<PackageInfo> packs = p.getInstalledPackages(0);
        List<PackageInfo> packs_permission = p.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        //This FOR goes through every app that is installed on the device according to
        // --> p.getInstalledPackages(0);
        for (int i =0; i<packs.size();i++){
            PackageInfo pinfo = packs.get(i); //This Var has the actual Information about an app (not the permission)
                    PackageInfo appPermission = packs_permission.get(i);
                    //If the App has NULL permissions then skip it
                    if (appPermission.requestedPermissions == null){
                        continue;
                    }
                    //Check if App has Internet Permission
                    for (String permission : appPermission.requestedPermissions) {
                        //Checking for Internet permission
                        if (TextUtils.equals(permission, android.Manifest.permission.INTERNET)) {
                            sortMap.put(pinfo.applicationInfo.loadLabel(getPackageManager()).toString(),pinfo.packageName);
                            break;
                        }
                    }
        }

        //Actual sorting alpabetic and convert into the String List "packageNames"
        Set set2 = sortMap.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()) {
            Map.Entry me2 = (Map.Entry)iterator2.next();
            packageNames.add(me2.getValue().toString());
        }

        return packageNames;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sortlist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // [See PFA Note APP]
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_alphabetical_asc) {
            sortAlphabetic_asc();
        } else if(id == R.id.action_sort_alphabetical_desc){
            sortAlphabetic_desc();
        } else if (id == R.id.action_sort_installdate_asc) {
            try {
                sortInstalledDate_asc();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else if(id == R.id.action_sort_installdate_desc){
            try {
                sortInstalledDate_desc();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        userInstalledAppsView = (ListView) findViewById(R.id.list_selection_app);
        AppListAdapter appAdapter = new AppListAdapter(this, app_list_name);
        userInstalledAppsView.setAdapter(appAdapter);

        return super.onOptionsItemSelected(item);
    }

    private void sortAlphabetic_asc() {
        app_list_name.clear();
        AscComparator comp = new AscComparator(sortMap);
        Map<String, String> newMap = new TreeMap(comp);
        newMap.putAll(sortMap);
        sortMap = newMap;

        for(Map.Entry<String,String> entry : sortMap.entrySet()) {
            app_list_name.add(entry.getValue().toString());
        }
    }

    private void sortAlphabetic_desc() {
        app_list_name.clear();
        DescComparator comp = new DescComparator(sortMap);
        Map<String, String> newMap = new TreeMap(comp);
        newMap.putAll(sortMap);
        sortMap = newMap;

        for(Map.Entry<String,String> entry : sortMap.entrySet()) {
            app_list_name.add(entry.getValue().toString());
        }
    }

    private void sortInstalledDate_asc() throws PackageManager.NameNotFoundException {
        Map<Long, String> appdates = new TreeMap<>();
        PackageManager packageManager = this.getPackageManager();
        for(int i = 0; i<app_list_name.size();i++){
            PackageInfo packageInfo = packageManager.getPackageInfo(app_list_name.get(i), 0);
            long installTimeInMilliseconds = packageInfo.firstInstallTime;
            appdates.put(installTimeInMilliseconds, app_list_name.get(i));
        }
        AscComparator comp = new AscComparator(appdates);
        Map<Long, String> newMap = new TreeMap(comp);
        newMap.putAll(appdates);
        appdates = newMap;
        app_list_name.clear();

        for(Map.Entry<Long,String> entry : appdates.entrySet()) {
            app_list_name.add(entry.getValue().toString());
        }
    }


    private void sortInstalledDate_desc() throws PackageManager.NameNotFoundException {
        Map<Long, String> appdates = new TreeMap<>();
        PackageManager packageManager = this.getPackageManager();
        for(int i = 0; i<app_list_name.size();i++){
            PackageInfo packageInfo = packageManager.getPackageInfo(app_list_name.get(i), 0);
            long installTimeInMilliseconds = packageInfo.firstInstallTime;
            appdates.put(installTimeInMilliseconds, app_list_name.get(i));
        }
        DescComparator comp = new DescComparator(appdates);
        Map<Long, String> newMap = new TreeMap(comp);
        newMap.putAll(appdates);
        appdates = newMap;
        app_list_name.clear();

        for(Map.Entry<Long,String> entry : appdates.entrySet()) {
            app_list_name.add(entry.getValue().toString());
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

}
