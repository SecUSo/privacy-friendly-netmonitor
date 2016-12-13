package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.TLType;
import org.secuso.privacyfriendlytlsmetric.Assistant.ToolBox;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Filter.Filter;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;

import static android.R.attr.name;
import static java.lang.Math.abs;

/**
 * Information class for aquired connection data. Full report on available information from device.
 */
public class Report implements Serializable {

    public Report(ByteBuffer bb , TLType type){
        touch();
        // Fill with bytebuffer data
        if (type == TLType.tcp || type == TLType.udp ){initIP4(bb); }
        else { initIP6(bb);}

        //Init InetAddresses
        try {
            localAdd = InetAddress.getByAddress(localAddHex);
            remoteAdd = InetAddress.getByAddress(remoteAddHex);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if(Const.IS_DEBUG) {
            Log.d(Const.LOG_TAG, "Report (" + type + "):" + localAdd.getHostAddress() + ":"
                    + localPort + " " + remoteAdd.getHostAddress() + ":" + remotePort + " - UID: " + uid);
        }
    }

    private TLType type;
    public Timestamp timestamp;

    private byte[] localAddHex;
    private InetAddress localAdd;
    private int localPort;

    private byte[] remoteAddHex;
    private InetAddress remoteAdd;

    private boolean remoteResolved;
    private int remotePort;

    private int pid;
    private int uid;

    public Drawable icon;
    private String appName;



    private String packageName;
    //TODO: Filters are not in use in passive anymore, implement suitable mechanism for passive scanning
    public Filter filter;

    //Set current timestamp
    public void touch(){ timestamp = new Timestamp(System.currentTimeMillis()); }

    // -----------------------
    // Init Methods
    // -----------------------

    //Fill report with Ip4 - tcp/udp connection data from bytebuffer readin
    private void initIP4(ByteBuffer bb) {
        bb.position(0);
        byte[] b = new byte[2];

        localAddHex = new byte[4];
        bb.get(localAddHex);
        localAddHex = ToolBox.reverseByteArray(localAddHex);
        bb.get(b);
        localPort = ToolBox.twoBytesToInt(b);

        remoteAddHex = new byte[4];
        bb.get(remoteAddHex);
        remoteAddHex = ToolBox.reverseByteArray(remoteAddHex);
        bb.get(b);
        remotePort = ToolBox.twoBytesToInt(b);

        uid = abs(bb.getShort());
    }
    //Fill report with Ip6 - tcp/udp connection data from bytebuffer readin
    private void initIP6(ByteBuffer bb) {
        bb.position(0);
        byte[] b = new byte[2];
        localAddHex = new byte[16];
        bb.get(localAddHex);
        bb.get(b);
        localPort = ToolBox.twoBytesToInt(b);
        remoteAddHex = new byte[16];
        bb.get(remoteAddHex);
        bb.get(b);
        remotePort = ToolBox.twoBytesToInt(b);
        uid = abs((bb.getShort()));
    }


    // getters and setters
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

    public InetAddress getRemoteAdd() { return remoteAdd; }

    public void setRemoteAdd(InetAddress remoteAdd) {
        this.remoteAdd = remoteAdd;
    }

    public Drawable getIcon() {return icon; }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) { this.appName = appName;}

    public TLType getType() {
        return type;
    }

    public void setType(TLType type) {
        this.type = type;
    }

    public byte[] getLocalAddHex() {
        return localAddHex;
    }

    public void setLocalAddHex(byte[] localAddHex) {
        this.localAddHex = localAddHex;
    }

    public InetAddress getLocalAdd() {
        return localAdd;
    }

    public void setLocalAdd(InetAddress localAdd) {
        this.localAdd = localAdd;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public byte[] getRemoteAddHex() {
        return remoteAddHex;
    }

    public void setRemoteAddHex(byte[] remoteAddHex) {
        this.remoteAddHex = remoteAddHex;
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

    public int getUid() { return uid; }

    public String getPackageName() { return packageName; }

    public void setPackageName(String packageName) { this.packageName = packageName; }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public boolean isRemoteResolved() { return remoteResolved; }

    public void setRemoteResolved(boolean remoteResolved) { this.remoteResolved = remoteResolved; }
}


