package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.graphics.drawable.Drawable;

import org.secuso.privacyfriendlytlsmetric.Assistant.TLType;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Filter.Filter;

import java.net.InetAddress;
import java.sql.Timestamp;

/**
 * Information class for aquired connection data. Full report on available information from device.
 */
public class Report {

    private TLType type;
    public Timestamp timestamp;

    private String localAddHex;
    private String loaclAddDec;
    private int localPort;

    private String remoteAddHex;
    private String remoteAddDec;
    private InetAddress remoteAdd;
    private int remotePort;

    private int pid;
    private int uid;

    public Drawable icon;
    private String packageName;

    //Filters are not in use, yet
    public Filter filter;

    //Set current timestamp
    public void touch(){
        timestamp = new Timestamp(System.currentTimeMillis());
    }



    // ------------------------
    // Getters and setters here
    // ------------------------
    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getRemoteAddDec() {
        return remoteAddDec;
    }

    public void setRemoteAddDec(String remoteAddDec) {
        this.remoteAddDec = remoteAddDec;
    }

    public InetAddress getRemoteAdd() {
        return remoteAdd;
    }

    public void setRemoteAdd(InetAddress remoteAdd) {
        this.remoteAdd = remoteAdd;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public TLType getType() {
        return type;
    }

    public void setType(TLType type) {
        this.type = type;
    }

    public String getLocalAddHex() {
        return localAddHex;
    }

    public void setLocalAddHex(String localAddHex) {
        this.localAddHex = localAddHex;
    }

    public String getLoaclAddDec() {
        return loaclAddDec;
    }

    public void setLoaclAddDec(String loaclAddDec) {
        this.loaclAddDec = loaclAddDec;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getRemoteAddHex() {
        return remoteAddHex;
    }

    public void setRemoteAddHex(String remoteAddHex) {
        this.remoteAddHex = remoteAddHex;
    }

    public String getRemoteAddDex() {
        return remoteAddDec;
    }

    public void setRemoteAddDex(String remoteAddDex) {
        this.remoteAddDec = remoteAddDex;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}


