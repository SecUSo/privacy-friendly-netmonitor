package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.TLType;
import org.secuso.privacyfriendlytlsmetric.Assistant.ToolBox;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;

import static java.lang.Math.abs;

/**
 * Information class for aquired connection data. Full report on available information from device.
 */
public class Report implements Serializable {

    public Report(ByteBuffer bb, TLType type) {
        touch();
        this.type = type;
        // Fill with bytebuffer data
        if (type == TLType.tcp || type == TLType.udp) {
            initIP4(bb);
        } else {
            initIP6(bb);
        }

        //Init InetAddresses
        try {
            if (type == TLType.tcp ||type == TLType.udp){
                localAdd = InetAddress.getByName(
                        ToolBox.hexToIp4(ToolBox.printHexBinary(localAddHex)));
                remoteAdd = InetAddress.getByName(
                        ToolBox.hexToIp4(ToolBox.printHexBinary(remoteAddHex)));
            } else {
                localAdd = InetAddress.getByName(
                        ToolBox.hexToIp6(ToolBox.printHexBinary(localAddHex)));
                remoteAdd = InetAddress.getByName(
                        ToolBox.hexToIp6(ToolBox.printHexBinary(remoteAddHex)));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (Const.IS_DEBUG) {
            Log.d(Const.LOG_TAG, "Report (" + type + "):" + localAdd.getHostAddress() + ":"
                    + localPort + " " + remoteAdd.getHostAddress() + ":" + remotePort + " - UID: " + uid);
        }
    }

    public TLType type;
    public Timestamp timestamp;

    public byte[] localAddHex;
    public InetAddress localAdd;
    public int localPort;
    public byte[] state;

    public byte[] remoteAddHex;
    public InetAddress remoteAdd;

    public boolean dnsIsResolved;
    public int remotePort;

    public int pid;
    public int uid;

    public Drawable icon;
    public String appName;

    public String packageName;

    //Set current timestamp
    public void touch() {
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    // -----------------------
    // Init Methods
    // -----------------------

    //Fill report with Ip4 - tcp/udp connection data from bytebuffer readin
    private void initIP4(ByteBuffer bb) {
        bb.position(0);
        byte[] b = new byte[2];
        localAddHex = new byte[4];
        bb.get(localAddHex);
        bb.get(b);
        localPort = ToolBox.twoBytesToInt(b);
        remoteAddHex = new byte[4];
        bb.get(remoteAddHex);
        bb.get(b);
        remotePort = ToolBox.twoBytesToInt(b);
        uid = abs(bb.getShort());
        state = new byte[1];
        bb.get(state);
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
        state = new byte[1];
        bb.get(state);
    }
}