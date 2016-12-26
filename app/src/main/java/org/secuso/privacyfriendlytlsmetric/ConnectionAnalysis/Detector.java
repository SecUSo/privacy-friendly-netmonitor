package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.ExecCom;
import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.Assistant.TLType;
import org.secuso.privacyfriendlytlsmetric.Assistant.ToolBox;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * Detects active connections on the device and identifies port-uid-pid realation. Corresponding
 * Apps are identified.
 */

class Detector {

    //Members
    //Get commands for shell readin
    private static final String commandTcp = "cat /proc/net/tcp";
    private static final String commandTcp6 = "cat /proc/net/tcp6";
    private static final String commandUdp = "cat /proc/net/udp";
    private static final String commandUdp6 = "cat /proc/net/udp6";

    static HashMap<Integer, Report> sReportMap = new HashMap<>();
    static boolean mIsLog = false;

    //Update the Report HashMap with current connections. Key = sourceport
    // Update strategies:
    // false = update and detach old
    // true = keep old
    static void updateReportMap(){
        updateOrAdd(getCurrentConnections());
        if (!mIsLog){ removeOldReports(); }
    }

    //Update existing or add new reports
    private static void updateOrAdd(ArrayList<Report> reportList){
        for (int i = 0; i < reportList.size(); i++) {
            //Key = source-Port
            int key = reportList.get(i).localPort;
            if(sReportMap.containsKey(key)){
                Report r = sReportMap.get(key);
                r.touch();
                r.state = reportList.get(i).state;
            } else{
                sReportMap.put(key,reportList.get(i));
            }
        }
    }

    //Remove timed-out connection-reports
    private static void removeOldReports() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RunStore.getContext());
        final int reportTTL =  prefs.getInt(Const.REPORT_TTL, Const.REPORT_TTL_DEFAULT);
        Timestamp thresh = new Timestamp(System.currentTimeMillis() - reportTTL) ;

        HashSet<Integer> keySet = new HashSet<>(sReportMap.keySet());
        for (int key:keySet) {
            if(sReportMap.get(key).timestamp.compareTo(thresh) < 0){
                sReportMap.remove(key);
            }
        }
    }

    // read the current connections off the designated files
    private static ArrayList<Report> getCurrentConnections(){
        ArrayList<Report> fullReportList = new ArrayList<>();

        //generate full report of all tcp/udp connections
        fullReportList.addAll(parseNetOutput(ExecCom.userForResult(commandTcp), TLType.tcp));
        fullReportList.addAll(parseNetOutput(ExecCom.userForResult(commandTcp6), TLType.tcp6));
        fullReportList.addAll(parseNetOutput(ExecCom.userForResult(commandUdp), TLType.udp));
        fullReportList.addAll(parseNetOutput(ExecCom.userForResult(commandUdp6), TLType.udp6));

        return fullReportList;
    }

    //TODO: old implementation - remove at time
    private static HashMap<Integer, Integer> mPortPidMap = new HashMap<>();
    private static HashMap<Integer, Integer> mUidPidMap = new HashMap<>();
    private static HashMap<Integer, Integer> mPortUidMap = new HashMap<>();

    //parse net output and scan for new conenctions, sort by port
    public static HashMap<Integer, Integer> getPortMap() {
        HashMap<Integer, Integer> result = new HashMap<>();

        return result;
    }


    //match pids
    public static void updateUidPidMap(){
        ActivityManager am = (ActivityManager) RunStore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if(!mUidPidMap.containsKey(info.uid)){
                mUidPidMap.put(info.uid, info.pid);
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Adding uid/pid: " + info.uid + " -> " + info.pid);
            }
        }
    }

    //match pid and uid, accessible by port
    private static void updatePortPidMap() {
        updateUidPidMap();
        Set<Integer> ports = mPortUidMap.keySet();
        for (int port :ports){
            if(!mPortPidMap.containsKey(port) && mUidPidMap.containsKey(mPortUidMap.get(port))){
                mPortPidMap.put(port, mUidPidMap.get(mPortUidMap.get(port)));
                if(Const.IS_DEBUG) Log.d(Const.LOG_TAG,"mapping port to pid: " + port + " ->" + mUidPidMap.get(mPortUidMap.get(port)));
            }
        }
    }

    //Parse output from /proc/net/tcp and udp files (ip4/6)
    public static List<Report> parseNetOutput(String readIn, TLType type) {
        String[] splitLines;
        LinkedList<Report> reportList = new LinkedList<>();

        splitLines = readIn.split("\\n");
        for (int i = 1; i < splitLines.length; i++) {
            splitLines[i] = splitLines[i].trim();
           reportList.add(initReport(splitLines[i], type));
        }
        return reportList;
    }

    public static Report initReport(String splitLine, TLType type){
        String splitTabs[];
        while (splitLine.contains("  ")) {
            splitLine = splitLine.replace("  ", " ");
        }
        splitTabs = splitLine.split("\\s");


        if (type == TLType.tcp || type == TLType.udp ){
            //Init IPv4 values
            return initReport4(splitTabs, type);
        } else {
            //Init IPv6 values
            return initReport6(splitTabs, type);
        }
    }

    //Init parsed data to IPv4 connection report
    private static Report initReport4(String[] splitTabs, TLType type){
        int pos;
        pos = 0;
        //Allocating buffer for 4 Bytes add and 2 bytes port each + 2 bytes UID
        ByteBuffer bb = ByteBuffer.allocate(15);
        bb.position(0);

        //local address
        String hexStr = splitTabs[1].substring(pos, pos + 8);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //local port
        pos = splitTabs[1].indexOf(":");
        hexStr = splitTabs[1].substring(pos +1, pos + 5);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //remote address
        pos = 0;
        hexStr = splitTabs[2].substring(pos, pos + 8);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //local port
        pos = splitTabs[2].indexOf(":");
        hexStr = splitTabs[2].substring(pos +1, pos + 5);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //UID
        bb.putShort(Short.parseShort(splitTabs[7]));

        //state
        bb.put(ToolBox.hexStringToByteArray(splitTabs[3]));

        return new Report(bb, type);
    }

    //Init parsed data to IPv6 connection report
    private static Report initReport6(String[] splitTabs, TLType type){
        int pos;
        pos = 0;
        //Allocating buffer for 16 Bytes add and 2 bytes port each + 2 bytes UID
        ByteBuffer bb = ByteBuffer.allocate(39);
        bb.position(0);

        //local address
        String hexStr = splitTabs[1].substring(pos, pos + 32);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //local port
        pos = splitTabs[1].indexOf(":");
        hexStr = splitTabs[1].substring(pos +1, pos + 5);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //remote address
        pos = 0;
        hexStr = splitTabs[2].substring(pos, pos + 32);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //local port
        pos = splitTabs[2].indexOf(":");
        hexStr = splitTabs[2].substring(pos +1, pos + 5);
        bb.put(ToolBox.hexStringToByteArray(hexStr));

        //UID
        short a = Short.parseShort(splitTabs[7]);
        bb.putShort(a);

        //state
        bb.put(ToolBox.hexStringToByteArray(splitTabs[3]));

        return new Report(bb, type);
    }

    //resolve an UID to a package name
    private static String getPackageName(int uid){
        final String command = "dumpsys package | grep -A1 'userid=" + uid + "'";

        return ExecCom.userForResult(command);
    }




}
