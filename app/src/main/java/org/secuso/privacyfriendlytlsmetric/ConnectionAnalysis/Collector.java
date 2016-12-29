package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.secuso.privacyfriendlytlsmetric.Assistant.AsyncDNS;
import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.Assistant.ToolBox;
import org.secuso.privacyfriendlytlsmetric.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
    static HashMap<Integer, PackageInfo> sCachePackage = new HashMap<>();
    static HashMap<Integer, Drawable> sCacheIcon = new HashMap<>();
    static HashMap<Integer, String> sCacheLabel = new HashMap<>();
    //ReportDetail information
    public static ArrayList<String[]> sDetailReportInfo;
    public static Report sDetailReport;
    //Data processing maps
    private static ArrayList<Report> sReportList;
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

        for (int i = 0; i < sReportList.size(); i++) {
            Report r = sReportList.get(i);

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
        sReportList = deepCloneReportList(reportList);
    }

    //Make an async reverse DNS request
    public static void resolveHosts() {
        for (Report r : sReportList){
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
        for (int i = 0; i < sReportList.size(); i++) {
            Report r = sReportList.get(i);
            if(!sCachePackage.containsKey(r.uid)) {
                updatePackageCache();
            }
            if(sCachePackage.containsKey(r.uid)){
                PackageInfo pi = sCachePackage.get(r.uid);
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
        sCachePackage = new HashMap();

        if(Const.IS_DEBUG){ printAllPackages(); }
        ArrayList<PackageInfo> infoList = (ArrayList<PackageInfo>) getPackages(RunStore.getContext());
        for (PackageInfo i : infoList) {
            if (i != null) {
                sCachePackage.put(i.applicationInfo.uid, i);
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
        if(!sCacheIcon.containsKey(uid)){
            if(sCachePackage.containsKey(uid)){
                sCacheIcon.put(uid, sCachePackage.get(uid).applicationInfo.
                        loadIcon(RunStore.getContext().getPackageManager()));
            } else {
                return RunStore.getContext().getDrawable(android.R.drawable.sym_def_app_icon);
            }
        }
        return sCacheIcon.get(uid);
    }

    public static String getLabel(int uid){
        if(!sCacheLabel.containsKey(uid)){
            if(sCachePackage.containsKey(uid)){
                sCacheLabel.put(uid, (String) sCachePackage.get(uid).applicationInfo.
                        loadLabel(RunStore.getContext().getPackageManager()));
            }
            else {
                return RunStore.getContext().getString(R.string.unknown_app);
            }
        }
        return sCacheLabel.get(uid);
    }

    public static String getPackage(int uid) {
        if(sCachePackage.containsKey(uid)) {
            return sCachePackage.get(uid).packageName;
        } else{
            return RunStore.getContext().getString(R.string.unknown_package);
        }
    }

    public static void provideDetail(int uid, byte[] remoteAddHex) {
        ArrayList<Report> filterList = filterReportsByAdd(uid, remoteAddHex);
        sDetailReport = filterList.get(0);
        buildDetail(filterList);
    }

    private static ArrayList<Report> filterReportsByAdd(int uid, byte[] remoteAddHex){
        List<Report> reportList = mUidReportMap.get(uid);
        ArrayList<Report> filterList = new ArrayList<>();
        for (int i = 0; i < reportList.size(); i++){
            if (Arrays.equals(reportList.get(i).remoteAddHex, remoteAddHex)){
                filterList.add(reportList.get(i));
            }
        }
        return filterList;
    }

    private static void buildDetail(ArrayList<Report> filterList) {
        ArrayList<String[]> l = new ArrayList<>();
        Report r = filterList.get(0);

        l.add(new String[]{"Remote Address", r.remoteAdd.getHostAddress()});
        l.add(new String[]{"Remote Address(HEX)", ToolBox.printHexBinary(r.remoteAdd.getAddress())});
        l.add(new String[]{"Remote Port", "" + r.remotePort});
        if(r.remoteResolved){ l.add(new String[]{"Remote Host", r.remoteAdd.getHostName()});}
        else { l.add(new String[]{"Remote Host", "name not resolved"}); }
        l.add(new String[]{"Layer4 Protocol", "" + r.type});
        l.add(new String[]{"Local Address", r.localAdd.getHostAddress()});
        l.add(new String[]{"Local Address(HEX)", ToolBox.printHexBinary(r.localAdd.getAddress())});

        l.add(new String[]{"Last Seen", r.timestamp.toString()});
        l.add(new String[]{"Last Seen", getTransportState(r.state)});

        l.add(new String[]{"", ""});
        l.add(new String[]{"Simultaneous Connections", "" + filterList.size()});
        for (int i = 0; i < filterList.size(); i++){
            Report r2 = filterList.get(i);
            l.add(new String[]{"    " + (i + 1) + " src port > dst port",
                    r2.localPort + " > " + r2.remotePort});
        }

        sDetailReportInfo = l;
    }


    private static String getTransportState(byte[] state) {

        String status;
        switch (ToolBox.printHexBinary(state)) {
            case "01":
                    status = "TCPF_ESTABLISHED";
            case "2":
                status = "TCPF_SYN_SENT";
            case "3":
                status = "TCPF_SYN_RECV";
            case "4":
                status = "TCPF_FIN_WAIT1";
            case "5":
                status = "TCPF_FIN_WAIT2";
            case "6":
                status = "TCPF_TIME_WAIT";
            case "7":
                status = "TCPF_CLOSE";
            case "8":
                status = "TCPF_CLOSE_WAIT";
            case "9":
                status = "TCPF_LAST_ACK";
            case "A":
                status = "TCPF_LISTEN";
            case "B":
                status = "TCPF_CLOSING";
            case "C":
                status = "TCPF_NEW_SYN_RECV";
            default:
                status = "UNDEFINED";
        }
        return status;

    }

}
