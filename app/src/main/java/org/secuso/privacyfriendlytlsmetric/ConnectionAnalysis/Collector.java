package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.ContextStorage;
import org.secuso.privacyfriendlytlsmetric.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Collector class collects data from the services and processes it for inter process communication
 * with the UI.
 */
public class Collector {

    //public Member for collecting non-serializable packet information like icons
    public static HashMap<String, PackageInformation> mPackageMap = new HashMap<>();

    //Data processing maps
    private static ArrayList<Report> mReportList;
    private static HashMap<String, List<String>> mReportsByApp;
    private static HashMap<Integer, PackageInformation> mUidPackageMap = new HashMap<>();

    //Pushed the newest availiable information as deep copy.
    public static HashMap<String, List<String>> provideReports(){

        //update reports
        pull();
        //process reports
        processPassive();
        //sorting
        sortReportsToMap();
        //update package info
        updatePI();

        return mReportsByApp;
    }

    private static void updatePI() {
        for (Integer i : mUidPackageMap.keySet()){
            PackageInformation pi = mUidPackageMap.get(i);
            mPackageMap.put(pi.packageName, pi);
        }
    }

    //Sorts the reports by app package name to a HashMap
    private static void sortReportsToMap() {
        mReportsByApp = new HashMap<>();

        for(int i = 0; i < mReportList.size(); i++) {
            Report r = mReportList.get(i);

            if (!mReportsByApp.containsKey(r.getPackageName())) {
                mReportsByApp.put(r.getPackageName(), new ArrayList<String>());
            }
            mReportsByApp.get(r.getPackageName()).add("" + r.getRemoteAdd() + r.getRemotePort());
        }

    }


    //pull records from detector and make a deep copy for frontend - usage
    private static void pull() {
        ArrayList<Report> reportList = new ArrayList<>();
        Set<Integer> keySet = Detector.sReportMap.keySet();
        for(int i : keySet){
            reportList.add(Detector.sReportMap.get(i));
        }
        mReportList = deepCloneReportList(reportList);
    }

    //Process all Records in the List.
    private static void processPassive() {
        fillPackageInformation();
        resolveHosts();
    }

    private static void resolveHosts() {
        //TODO: implement, use permission swich
    }

    private static void fillPackageInformation(){
        //Get Package Information
        for (int i = 0; i < mReportList.size(); i++){
            Report report = mReportList.get(i);
            updatePackage(report.getUid());
            PackageInformation pi = mUidPackageMap.get(report.getUid());
            report.setPid(pi.pid);
            report.setPackageName(pi.packageName);
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

    //Updates the PackageInformation hash map with new entries.
    private static void updatePackage(int uid) {
        if (!mUidPackageMap.containsKey(uid)) {

            //Generate system PackageInformation
            PackageInformation pi = new PackageInformation();
            PackageManager pm = ContextStorage.getContext().getPackageManager();
            ActivityManager am = (ActivityManager) ContextStorage.getContext().getSystemService(Context.ACTIVITY_SERVICE);

            List<ActivityManager.RunningAppProcessInfo> activeApps = am.getRunningAppProcesses();
            for (int i = 0; i < activeApps.size(); i++) {
                ActivityManager.RunningAppProcessInfo info = activeApps.get(i);
                if (info.uid == uid) {
                    try {
                        String[] list = info.pkgList;
                        pi.packageName = list[0];
                        // debug print of found package
                        if (Const.IS_DEBUG) {
                            String pkg = "";
                            for (int j = 0; j < list.length; j++) {
                                pkg = pkg + list[j];
                            }
                            Log.d(Const.LOG_TAG, pkg);
                        }

                        pi.uid = uid;
                        pi.pid = info.pid;
                        pi.icon = pm.getApplicationIcon(pi.packageName);
                        mUidPackageMap.put(uid, pi);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    switch (uid) {
                        case 0:
                            pi.uid = uid;
                            pi.pid = 0;
                            pi.packageName = "android.system";
                            pi.icon = ContextStorage.getContext().getDrawable(android.R.drawable.sym_def_app_icon);
                            break;

                        case 1000:
                            pi.uid = uid;
                            pi.pid = 0;
                            pi.packageName = "system.root";
                            //TODO: Change icons
                            pi.icon = ContextStorage.getContext().getDrawable(R.drawable.unknown_app_web);
                            break;
                        default:
                            pi.uid = -1;
                            pi.pid = 0;
                            pi.packageName = "Unknown App";
                            //TODO: Change icons
                            pi.icon = ContextStorage.getContext().getDrawable(R.drawable.unknown_app_web);
                            break;
                    }
                    mUidPackageMap.put(uid, pi);
                }
            }
        }
    }
}
