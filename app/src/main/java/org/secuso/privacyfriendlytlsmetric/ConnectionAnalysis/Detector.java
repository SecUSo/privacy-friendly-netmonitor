package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.ContextStorage;
import org.secuso.privacyfriendlytlsmetric.Assistant.ExecuteCommand;
import org.secuso.privacyfriendlytlsmetric.Assistant.TLType;
import org.secuso.privacyfriendlytlsmetric.Assistant.ToolBox;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Detects active connections on the device and identifies port-uid-pid realation. Corresponding
 * Apps are identified.
 */

public class Detector {

    //Members
    public static HashMap<Integer, Report> sReportMap = new HashMap<>();

    private static int mUpdateType = 0;

    //Update the Report HashMap with current connections. Key = sourceport
    //The int defines the update strategy:
    // 0 = overwrite and append
    // 1 = append
    // 2 = detach old
    public static void updateReportMap(){
        LinkedList<Report> reportList = getCurrentConnections();

        switch (mUpdateType){
            case 0:
                for (Report r:reportList) {
                    //Key = source-Port
                    int key = r.getLocalPort();
                    if(sReportMap.containsKey(key)){
                        sReportMap.remove(key);
                        sReportMap.put(key,r);
                    } else{
                        sReportMap.put(key,r);
                    }
                }
                break;
            case 1:
                for (Report r:reportList) {
                    //Key = source-Port
                    int key = r.getLocalPort();
                    if(!sReportMap.containsKey(key)){
                        sReportMap.put(key,r);
                    }
                }
                break;
            case 2:
                sReportMap = new HashMap<>();
                for (Report r:reportList) {
                    int key = r.getLocalPort();
                    sReportMap.put(key,r);
                }
        }
    }

    public static int getmUpdateType() {
        return mUpdateType;
    }

    public static void setmUpdateType(int mUpdateType) {
        Detector.mUpdateType = mUpdateType;
    }

    public static LinkedList<Report> getCurrentConnections(){
        LinkedList<Report> fullReportList = new LinkedList<Report>();

        //Get commands for shell readin
        String commandTcp = "cat /proc/net/tcp";
        String commandTcp6 = "cat /proc/net/tcp6";
        String commandUdp = "cat /proc/net/udp";
        String commandUdp6 = "cat /proc/net/udp6";

        //generate full report of all tcp/udp connections
        fullReportList.addAll(parseNetOutput(ExecuteCommand.userForResult(commandTcp), TLType.tcp));
        fullReportList.addAll(parseNetOutput(ExecuteCommand.userForResult(commandTcp6), TLType.tcp6));
        fullReportList.addAll(parseNetOutput(ExecuteCommand.userForResult(commandUdp), TLType.udp));
        fullReportList.addAll(parseNetOutput(ExecuteCommand.userForResult(commandUdp6), TLType.udp6));

        return fullReportList;
    }

    //TODO: old implementation - remove at time
    private static HashMap<Integer, Integer> mPortPidMap = new HashMap<>();
    private static HashMap<Integer, Integer> mUidPidMap = new HashMap<>();
    private static HashMap<Integer, Integer> mPortUidMap = new HashMap<>();
    public static HashMap<Integer, PackageInformation> mPacketInfoMap;

    //TODO: This is Debug -remove later
    public static void printParsedPorts(){
        updatePortUidMap();
    }

    //parse net output and scan for new conenctions, sort by port
    public static HashMap<Integer, Integer> getPortMap() {
        HashMap<Integer, Integer> result = new HashMap<>();

        return result;
    }

    public static int getPidByPort(int port) {
        if(!mPortPidMap.containsKey(port)){
            updatePortPidMap();
            if(mPortPidMap.containsKey(port)){
                return mPortPidMap.get(port);
            } else{
                return -1;
            }
        } else {
            return mPortPidMap.get(port);

        }
    }

    public static int getUidByPort(int port) {
        if(!mPortUidMap.containsKey(port)){
            updatePortUidMap();
            if(mPortUidMap.containsKey(port)){
                return mPortUidMap.get(port);
            } else{
                return -1;
            }
        } else {
            return mPortPidMap.get(port);

        }
    }

    private static void updatePortUidMap(){
        mPortUidMap = getPortMap();
    }

    //match pids
    public static void updateUidPidMap(){
        ActivityManager am = (ActivityManager) ContextStorage.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if(!mUidPidMap.containsKey(info.uid)){
                mUidPidMap.put(info.uid, info.pid);
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Adding uid/pid: " + info.uid + " -> " + info.pid);
            }
        }
    }

    //match pid and uid, accessiable by port
    private static void updatePortPidMap() {
        updatePortUidMap();
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
        ByteBuffer bb = ByteBuffer.allocate(14);
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

        return new Report(bb, type);
    }

    //Init parsed data to IPv6 connection report
    private static Report initReport6(String[] splitTabs, TLType type){
        int pos;
        pos = 0;
        //Allocating buffer for 16 Bytes add and 2 bytes port each + 2 bytes UID
        ByteBuffer bb = ByteBuffer.allocate(38);
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

        return new Report(bb, type);
    }


    //Updates the PackageInformation hash map with new entries.
    private static void updatePackageInformationData(int pid, int uid) {
        if (pid >= 0 && !mPacketInfoMap.containsKey(pid)){
            PackageManager pm = ContextStorage.getContext().getPackageManager();
            ActivityManager am = (ActivityManager) ContextStorage.getContext().getSystemService(Context.ACTIVITY_SERVICE);
            PackageInformation pi = new PackageInformation();
            pi.pid = pid;
            pi.uid = uid;

            List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
            for (int i = 0; i < pids.size(); i++) {
                ActivityManager.RunningAppProcessInfo info = pids.get(i);
                if((info.pid == pid || info.uid == uid )&& !mPacketInfoMap.containsKey(pid)){
                    try {
                        String[] list = info.pkgList;
                        if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Processing packet information of: " + list[0]);
                        pi.packageName = list[0];
                        pi.icon = pm.getApplicationIcon(pi.packageName);
                        mPacketInfoMap.put(pid, pi);
                        mPacketInfoMap.put(uid, pi);
                    } catch (PackageManager.NameNotFoundException e) {
                        if(Const.IS_DEBUG)Log.e(Const.LOG_TAG, "Icon and/or package name not found. Using TLSMetric icon for unknown app.");
                    }

                }
            }
        }
    }
}
