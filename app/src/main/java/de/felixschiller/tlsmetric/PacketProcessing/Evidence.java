/*
    TLSMetric
    - Copyright (2015, 2016) Felix Tsala Schiller

    ###################################################################

    This file is part of TLSMetric.

    TLSMetric is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TLSMetric is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TLSMetric.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von TLSMetric.

    TLSMetric ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    TLSMetric wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */


package de.felixschiller.tlsmetric.PacketProcessing;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.voytechs.jnetstream.codec.Header;
import com.voytechs.jnetstream.codec.Packet;
import com.voytechs.jnetstream.primitive.address.Address;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.felixschiller.tlsmetric.Assistant.Const;
import de.felixschiller.tlsmetric.Assistant.ContextSingleton;
import de.felixschiller.tlsmetric.Assistant.ExecuteCommand;
import de.felixschiller.tlsmetric.Assistant.ToolBox;
import de.felixschiller.tlsmetric.PacketProcessing.Filter.Empty;
import de.felixschiller.tlsmetric.PacketProcessing.Filter.Filter;
import de.felixschiller.tlsmetric.R;

/**
 * Class for generating connection information (Evidence Reports) from packets, which has been
 * detected by the Analyser Service.
 */
public class Evidence {
    //public Members
    public static ArrayList<Report> mEvidence;
    public static ArrayList<Report> mEvidenceDetail;
    public static HashMap<Integer, ArrayList<Report>> mEvidenceDetailMap;
    public static HashMap<Integer, PackageInformation> mPacketInfoMap;
    public static int newWarnings;

    //private Members
    private static HashMap<Integer, Integer> mPortPidMap = new HashMap<>();
    private static HashMap<Integer, Integer> mUidPidMap = new HashMap<>();
    private static HashMap<Integer, Integer> mPortUidMap = new HashMap<>();


    public Evidence(){
        mEvidence = new ArrayList<>();
        mEvidenceDetailMap = new HashMap<>();
        mPacketInfoMap = new HashMap<>();
        updateConnections();
        newWarnings = 0;
    }


    // Update currently active connection
    // TODO: UID/PID detection needs improvement. Unknown Apps exist
    public static void updateConnections(){
        updatePortPidMap();
        Set<Integer> ports = mPortPidMap.keySet();
            for(int i =0; i< mEvidence.size(); i++){
                int con = mEvidence.get(i).srcPort;
                if (ports.contains(con)){
                    ports.remove(con);
                }
            }
        for (int port: ports) {
            Report ann = new Report();
            ann.filter = new Empty(Filter.Protocol.UNKNOWN,-1,"SrcPort: " + port + "No data.");
            ann.touch();
            ann.srcPort = port;
            ann.pid = getPidByPort(port);
            updatePackageInformationData(ann.pid, ann.uid);
            //TODO: parse url from /proc/net/tcp
            ann.url = "unknown";
            addEvidenceEntry(ann);
        }
   }

    // Filter triggered? -> Generate Report
    public boolean processPacket(Packet pkt) {
        Filter filter = scanPacket(pkt);
        if (filter != null) {
            if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Filter triggered: " + filter.protocol);
            Report ann = generateReport(pkt, filter);
            addEvidenceEntry(ann);
            return true;
        } else {
            return false;
        }

    }

    // Sort a Reports in the Lists
    private static void addEvidenceEntry(Report ann){

        boolean updated = false;

        //Check and update existing connections with lesser filter severity (unknown (-1) or ok (0))
        for(int i =0; i< mEvidence.size(); i++){
            if(mEvidence.get(i).srcPort == ann.srcPort){
                updated = true;
                if(mEvidence.get(i).filter.severity < ann.filter.severity){
                    if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Replacing connection to " + ann.url + " in evidence list. Higher warning state.");
                    mEvidence.set(i, ann);
                    //Set notification count +1
                    if(ann.filter.severity > 0){ newWarnings++;}
                }
            }
        }

        //Add found filters if connection not yet exist
        if(!updated){
            if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Adding connection " + ann.url + "to evidence list.");
            mEvidence.add(ann);
            //Set notification count +1
            if(ann.filter.severity > 0){ newWarnings++;}
        }

        //Add found filters to detail list, if triggered filter not already exist.
        if(mEvidenceDetailMap.containsKey(ann.srcPort)){
            ArrayList<Report> detailList = mEvidenceDetailMap.get(ann.srcPort);
            boolean hasFilter = false;
            for(Report exAnn : detailList){
                if(exAnn.filter.getClass() == ann.filter.getClass()){
                    exAnn.touch();
                    hasFilter = true;
                }
            }
            if(!hasFilter){
                detailList.add(ann);
            }
        } else {
            ArrayList<Report> newList = new ArrayList<>();
            newList.add(ann);
            mEvidenceDetailMap.put(ann.srcPort, newList);
        }
    }

    //Scan a packet for TCP payload and initiate identification
    //TODO: Move alle detection methods in new module when new filter system is designed
    private Filter scanPacket(Packet pkt) {

        if (pkt.hasHeader("TCP") && pkt.hasDataHeader()) {
            byte[] b = pkt.getDataValue();
            //if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, b.length + " Bytes data found");
            if (b.length > 0){
                ByteBuffer bb = ByteBuffer.allocate(b.length);
                bb.put(b);

                byte[] identChunk;
                if (b.length >= 12){
                    identChunk = new byte[20];
                } else {
                    identChunk = new byte[b.length];
                }

                bb.position(0);
                try {
                    bb.get(identChunk);
                }catch (BufferOverflowException |BufferUnderflowException e) {
                    Log.e(Const.LOG_TAG, "Could not read identChunk from TCP packet.");
                }
                return Identifyer.indent(identChunk);
            } else return null;
        } else {
            return null;
        }
    }

    // generate an evidence report.
    public static Report generateReport(Packet pkt, Filter filter) {
        Report ann = new Report();
        ann.filter = filter;
        ann.touch();
        fillConnectionData(ann, pkt);
        ann.uid = getUidByPort(ann.srcPort);
        ann.pid = getPidByPort(ann.srcPort);
        updatePackageInformationData(ann.pid, ann.uid);
        return ann;
    }

    //Extract connection details from packet
    private static void fillConnectionData(Report ann, Packet pkt) {
        Header ipHeader;
        Header transportHeader;
        ann.timestamp = new Timestamp(System.currentTimeMillis());
        //Read ip and transport Header
        if (pkt.hasHeader("IPv4")) {
            ipHeader = pkt.getHeader("IPv4");
        } else if (pkt.hasHeader("IPv6")){
            ipHeader = pkt.getHeader("IPv6");
        } else {
            ipHeader = null;
        }

        if (pkt.hasHeader("TCP") && ipHeader != null) {
            transportHeader = pkt.getHeader("TCP");
        }else if (pkt.hasHeader("UDP") && ipHeader != null ) {
            transportHeader = pkt.getHeader("UDP");
        } else {
            transportHeader = null;
        }

        if(ipHeader != null && transportHeader != null) {
            try {
            //Get ports and addresses from header
            Address address = (Address)ipHeader.getValue("daddr");
            InetAddress remoteaddress = InetAddress.getByAddress(address.toByteArray());
                if(getPortMap().containsKey((int) transportHeader.getValue("sport"))){
                    ann.dstAddr = remoteaddress;
                    ann.dstPort = (int) transportHeader.getValue("dport");
                    ann.srcPort = (int) transportHeader.getValue("sport");
                } else {
                    address = (Address)ipHeader.getValue("saddr");
                    ann.dstAddr = InetAddress.getByAddress(address.toByteArray());
                    ann.srcPort = (int) transportHeader.getValue("dport");
                    ann.dstPort = (int) transportHeader.getValue("sport");
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if(ann.dstAddr != null)ann.url = ann.dstAddr.getHostName();
        }
    }

    //Updates the PackageInformation hash map with new entries.
    private static void updatePackageInformationData(int pid, int uid) {
        if (pid >= 0 && !mPacketInfoMap.containsKey(pid)){
            PackageManager pm = ContextSingleton.getContext().getPackageManager();
            ActivityManager am = (ActivityManager) ContextSingleton.getContext().getSystemService(Context.ACTIVITY_SERVICE);
            PackageInformation pi = generateDummy();
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

    //Remove all reports of inactive connection
    public static void disposeInactiveEvidence(){
        for (int i = 0; i < mEvidence.size(); i++){
            if(!Evidence.mPortUidMap.containsKey(mEvidence.get(i).srcPort)){
                mEvidenceDetailMap.remove(mEvidence.get(i).srcPort);
                mEvidence.remove(i);
            }
        }
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

    //match pid and uid, accessiable by port
    private static void updatePortPidMap() {
        updateUidPidMap();
        updatePortUidMap();
        Set<Integer> ports = mPortUidMap.keySet();
        for (int port :ports){
            if(!mPortPidMap.containsKey(port) && mUidPidMap.containsKey(mPortUidMap.get(port))){
                mPortPidMap.put(port, mUidPidMap.get(mPortUidMap.get(port)));
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG,"mapping port to pid: " + port + " ->" + mUidPidMap.get(mPortUidMap.get(port)));
            }
        }
    }

    //match pids
    public static void updateUidPidMap(){
        ActivityManager am = (ActivityManager) ContextSingleton.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if(!mUidPidMap.containsKey(info.uid)){
                mUidPidMap.put(info.uid, info.pid);
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Adding uid/pid: " + info.uid + " -> " + info.pid);
            }
        }
    }

    //parse net output and scan for new conenctions, sort by port
    public static HashMap<Integer, Integer> getPortMap() {
        HashMap<Integer, Integer> result = new HashMap<>();
        String commandTcp4 = "cat /proc/net/tcp";
        String commandTcp6 = "cat /proc/net/tcp6";

        parseNetOutput(ExecuteCommand.userForResult(commandTcp4), result);
        parseNetOutput(ExecuteCommand.userForResult(commandTcp6), result);

        return result;
    }

    //Parse output from /proc/net/tcp
    public static void parseNetOutput(String readIn, HashMap<Integer, Integer> hashMap) {
        String[] splitLines;
        String[] splitTabs;

        splitLines = readIn.split("\\n");
        for (int i = 1; i < splitLines.length; i++) {
            splitLines[i] = splitLines[i].trim();
            while (splitLines[i].contains("  ")) {
                splitLines[i] = splitLines[i].replace("  ", " ");
            }
            splitTabs = splitLines[i].split("\\s");
            int pos = splitTabs[1].indexOf(":");
            String port = splitTabs[1].substring(pos + 1, pos + 5);

            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.position(2);
            bb.put(ToolBox.hexStringToByteArray(port));
            bb.position(0);
            int srcPort = bb.getInt();
            int uid = Integer.parseInt(splitTabs[7]);
            hashMap.put(srcPort, uid);
            if (Const.IS_DEBUG)Log.d(Const.LOG_TAG,"port to uid:" + srcPort + " -> " + uid);
        }
    }

    // request PacketInformation
    public static PackageInformation getPackageInformation(int pid,int uid) {
        if(mPacketInfoMap.containsKey(pid)){
            return mPacketInfoMap.get(pid);
        } else if(mPacketInfoMap.containsKey(uid)) {
            return mPacketInfoMap.get(uid);
        }else {
            updatePackageInformationData(pid, uid);
            if(mPacketInfoMap.containsKey(pid)){
                return mPacketInfoMap.get(pid);
            } else if(mPacketInfoMap.containsKey(uid)) {
                return mPacketInfoMap.get(uid);
            }else {
                return generateDummy();
            }
        }

    }

    //Just a BubbleSort - order ArrayList<Report> in place by by severity, DESC
    private static void sortAnnList(ArrayList<Report> annList){
        int range = annList.size() - 1;
        while(range > 1){
            for(int i = 0; i < range; i ++){
                if(annList.get(i).filter.severity < annList.get(i + 1).filter.severity){
                    Report tmpAnn = annList.get(i);
                    annList.set(i, annList.get(i + 1));
                    annList.set(i + 1, tmpAnn);
                }
            }
            range --;
        }
    }

    //Sort the report list and return it report
    //TODO: deep copy
    public static ArrayList<Report> getSortedEvidence(){
        sortAnnList(mEvidence);
        return mEvidence;
    }

    //Sort the report detail list and return it report
    //TODO: deep copy
    public static void setSortedEvidenceDetail(int key){
        sortAnnList(mEvidenceDetailMap.get(key));
        mEvidenceDetail = mEvidenceDetailMap.get(key);
    }

    //No UID/PID match? Go dummy!
    private static PackageInformation generateDummy() {
        PackageInformation pi = new PackageInformation();
        pi.icon = ContextSingleton.getContext().getResources().getDrawable(R.mipmap.unknown_app);
        pi.packageName = "Unknown App";
        pi.pid = -1;
        pi.uid = -1;
        return pi;
    }

    // Get highest severity level in list
    public static int getMaxSeverity(){
        int severity = -1;
        for(Report ann : mEvidence){
            if(ann.filter.severity > severity){
                severity = ann.filter.severity;
            }
        }
        return severity;
    }

    //For further use:
    //Example method for parsing /proc/pid/output
    /*    public static HashMap<Integer, Integer> generatePortPidMap(){
        if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Generating Port-to-Pid Map.");
        HashMap<Integer, Integer> portPidMap = new HashMap<>();
        HashMap<Integer, Integer> portUidMap = getPortMap();

        Set<Integer> set = portUidMap.keySet();
        for (int key : set) {
            int uid = portUidMap.get(key);
            if(mUidPidMap.containsKey(uid)){
                portPidMap.put(key, mUidPidMap.get(uid));
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "PortPidMap matched uid " + uid +
                        "->" + key + ", " + mUidPidMap.get(uid) );
            } else if(uid == 0){
                portPidMap.put(key, 0);
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Root uid " + uid +
                        ": " + key + ", " + 0 );
            } else {
                portPidMap.put(key, -1);
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Could not match by uid " + uid +
                        ": " + key + ", " + -1 );
            }
        }
    return portPidMap;
    }*/

    /*
    public static HashMap<Integer, Integer> getPidMap(){
        HashMap<Integer, Integer> result = new HashMap<>();
        int[] pids = getPids();

        String[] split;
        for (int pid : pids) {
            String command = "cat /proc/" + pid + "/status";
            String readIn = ExecuteCommand.userForResult(command);
            int pos = readIn.indexOf("Uid:");
            try {
                readIn = readIn.substring(pos, pos + 20);
            } catch (StringIndexOutOfBoundsException e){
                Log.e(Const.LOG_TAG, "Readin of uid of process " + pid + " failed, StringIndexOutOfBounds.");
            }

            split = readIn.split("\\t");
            if(split.length > 1) {
                try {
                    int uid = Integer.parseInt(split[1]);
                    Log.d(Const.LOG_TAG, "pid to uid: " + pid + "->" + uid);
                    result.put(uid, pid);
                } catch (NumberFormatException e) {
                    Log.e(Const.LOG_TAG, "Parsing of UID failed! " + split[1] + " Pid: " + pid);
                    result.put(-1, pid);
                }
            }
        }

        return result;
    }*/

}
