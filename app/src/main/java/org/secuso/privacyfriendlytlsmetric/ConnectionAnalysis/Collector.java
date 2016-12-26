package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.util.Log;

import org.secuso.privacyfriendlytlsmetric.Assistant.AsyncDNS;
import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collector class collects data from the services and processes it for inter process communication
 * with the UI.
 */
public class Collector {

    //application info caches
    public static HashMap<Integer, PackageInfo> mCachePackage = new HashMap<>();
    public static HashMap<Integer, Drawable> mCacheIcon = new HashMap<>();
    public static HashMap<Integer, String> mCacheLabel = new HashMap<>();

    //Data processing maps
    private static ArrayList<Report> mReportList;
    private static HashMap<Integer, List<Report>> mUidReportMap;

    //Pushed the newest availiable information as deep copy.
    public static HashMap<Integer, List<Report>> provideSimpleReports(){
        updateReports();
        return filterReports();
    }

    public static HashMap<Integer, List<Report>> provideFullReports() {
        updateReports();
        return mUidReportMap;
    }

    //Generate an overview List, with only one report per remote address per app
    private static HashMap<Integer, List<Report>> filterReports() {
        HashMap<Integer, List<Report>> filteredReportsByApp = new HashMap<>();
        HashSet<String> filterMap = new HashSet<>();
        String address;
        ArrayList<Report> list;
        ArrayList<Report> filteredList;
        for (int key : mUidReportMap.keySet()) {
            filteredReportsByApp.put(key, new ArrayList<Report>());
            list = (ArrayList<Report>) mUidReportMap.get(key);
            filteredList = (ArrayList<Report>) filteredReportsByApp.get(key);
            filterMap.clear();
            for (int i = 0; i < list.size(); i++) {
                address = list.get(i).remoteAdd.getHostAddress();
                if (!filterMap.contains(address)) {
                    filteredList.add(list.get(i));
                    filterMap.add(address);
                }
            }
        }
        return filteredReportsByApp;
    }

    private static void updateReports(){
        //update reports
        pull();
        //process reports (passive mode)
        fillPackageInformation();
        //resolve remote hosts (in cache or permission.INTERNET required)
        new AsyncDNS().execute("");
        //sorting
        sortReportsToMap();
    }

    //Sorts the reports by app package name to a HashMap
    private static void sortReportsToMap() {
        mUidReportMap = new HashMap<>();

        for (int i = 0; i < mReportList.size(); i++) {
            Report r = mReportList.get(i);

            if (!mUidReportMap.containsKey(r.uid)) {
                mUidReportMap.put(r.uid, new ArrayList<Report>());
            }
            mUidReportMap.get(r.uid).add(r);
        }
    }


    //pull records from detector and make a deep copy for frontend - usage
    private static void pull() {
        ArrayList<Report> reportList = new ArrayList<>();
        Set<Integer> keySet = Detector.sReportMap.keySet();
        for (int i : keySet) {
            reportList.add(Detector.sReportMap.get(i));
        }
        mReportList = deepCloneReportList(reportList);
    }

    //Make an async reverse DNS request
    public static void resolveHosts() {
        for (Report r : mReportList){
            try {
                r.remoteAdd.getHostName();
                r.remoteResolved = true;
            } catch(RuntimeException e) {
                r.remoteResolved = false;
                Log.e(Const.LOG_TAG, "Attempt to resolve host name failed");
                e.printStackTrace();
            }
        }
    }

    private static void fillPackageInformation() {
        for (int i = 0; i < mReportList.size(); i++) {
            Report r = mReportList.get(i);
            if(!mCachePackage.containsKey(r.uid)) {
                updatePackageCache();
            }
            if(mCachePackage.containsKey(r.uid)){
                PackageInfo pi = mCachePackage.get(r.uid);
                r.appName = pi.applicationInfo.name;
                r.packageName = pi.packageName;
            } else {
                r.appName = "Unknown App";
                r.appName = "app.unknown";
            }
        }
    }

    //Make a deep copy of the report list
    private static ArrayList<Report> deepCloneReportList(ArrayList<Report> reportList) {
        ArrayList<Report> cloneList = new ArrayList<>();
        try {
            for (int i = 0; i < reportList.size(); i++) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteOut);
                out.writeObject(reportList.get(i));
                out.flush();
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
                cloneList.add(Report.class.cast(in.readObject()));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cloneList;
    }

    //Updates the PkgInfo hash map with new entries.
    private static void updatePackageCache() {
        mCachePackage = new HashMap();

        if(Const.IS_DEBUG){ printAllPackages(); }
        ArrayList<PackageInfo> infoList = (ArrayList<PackageInfo>) getPackages(RunStore.getContext());
        for (PackageInfo i : infoList) {
            if (i != null) {
                mCachePackage.put(i.applicationInfo.uid, i);
            }
        }
    }

    private static List<PackageInfo> getPackages(Context context) {
        synchronized (context.getApplicationContext()) {
                PackageManager pm = context.getPackageManager();
            return new ArrayList<>(pm.getInstalledPackages(0));
        }
    }

    //degub print: Print all reachable active processes
    private static void printAllPackages() {
            ArrayList<PackageInfo> infoList = (ArrayList<PackageInfo>) getPackages(RunStore.getContext());
            for (PackageInfo i : infoList) {
                Log.d(Const.LOG_TAG, i.packageName + " uid_" + i.applicationInfo.uid);
            }
    }

    public static Drawable getIcon(int uid){
        if(!mCacheIcon.containsKey(uid)){
            if(mCachePackage.containsKey(uid)){
                mCacheIcon.put(uid, mCachePackage.get(uid).applicationInfo.
                        loadIcon(RunStore.getContext().getPackageManager()));
            } else {
                return RunStore.getContext().getDrawable(android.R.drawable.sym_def_app_icon);
            }
        }
        return mCacheIcon.get(uid);
    }

    public static String getLabel(int uid){
        if(!mCacheLabel.containsKey(uid)){
            if(mCachePackage.containsKey(uid)){
                mCacheLabel.put(uid, (String)mCachePackage.get(uid).applicationInfo.
                        loadLabel(RunStore.getContext().getPackageManager()));
            }
            else {
                return RunStore.getContext().getString(R.string.unknown_app);
            }
        }
        return mCacheLabel.get(uid);
    }

    public static String getPackage(int uid) {
        if(mCachePackage.containsKey(uid)) {
            return mCachePackage.get(uid).packageName;
        } else{
            return RunStore.getContext().getString(R.string.unknown_package);
        }
    }
}
