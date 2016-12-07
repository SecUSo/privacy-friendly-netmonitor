package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.ContextStorage;

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

    public static Report[] mReportArray;
    public static HashMap<Integer, PackageInformation> mUidPackageMap = new HashMap<>();

    private static ArrayList<Report> mReportList;

    //Pushed the newest availiable information as deep copy.
    public static void provideReports(){
        updateReportList();

        mReportArray = new Report[mReportList.size()];
        for (int i = 0; i < mReportList.size(); i++){
            mReportArray[i] = mReportList.get(i);
        }
    }

    public static void updateReportList(){
        pull();
        processPassive();
    }

    //pull records from detector and make a deep copy for frontend
    private static void pull() {
        ArrayList<Report> reportList = new ArrayList<>();
        Set<Integer> keySet = Detector.sReportMap.keySet();
        for(int i : keySet){
            reportList.add(Detector.sReportMap.get(i));
        }
        mReportList = deepCloneReportList(reportList);
    }

    //Process all Records in the List, based on a passive service
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
        if (!mUidPackageMap.containsKey(uid)){

            //Generate System PackageInformation
            PackageInformation pi = new PackageInformation();
            if(uid == 0){
                pi.uid = 0;
                pi.pid = 0;
                pi.packageName = "android.system";
                //TODO: find icon for pi.icon
                mUidPackageMap.put(uid, pi);
            } else {
                PackageManager pm = ContextStorage.getContext().getPackageManager();
                ActivityManager am = (ActivityManager) ContextStorage.getContext().getSystemService(Context.ACTIVITY_SERVICE);


                pi.uid = uid;

                List<ActivityManager.RunningAppProcessInfo> activeApps = am.getRunningAppProcesses();
                for (int i = 0; i < activeApps.size(); i++) {
                    ActivityManager.RunningAppProcessInfo info = activeApps.get(i);
                    if (info.uid == uid) {
                        try {
                            String[] list = info.pkgList;
                            pi.packageName = list[0];
                            pi.pid = info.pid;
                            pi.icon = pm.getApplicationIcon(pi.packageName);
                            mUidPackageMap.put(uid, pi);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }
}
