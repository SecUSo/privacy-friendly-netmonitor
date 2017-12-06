package org.secuso.privacyfriendlynetmonitor.DatabaseUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by m4rc0 on 08.11.2017.
 */

@Entity(
        nameInDb = "REPORTS",
        indexes = {
                @Index(value = "id DESC", unique = true)
        })
public class ReportEntity {

    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String appName;

    @NotNull
    private  String userID;
    @NotNull
    private  String appVersion;
    @NotNull
    private  String installedOn;

    @NotNull
    private  String remoteAddress;
    @NotNull
    private  String remoteHex;
    @NotNull
    private  String remoteHost;
    @NotNull
    private  String localAddress;
    @NotNull
    private  String localHex;

    @NotNull
    private  String servicePoint;
    @NotNull
    private  String payloadProtocol;
    @NotNull
    private  String transportProtocol;
    @NotNull
    private  String lastSeen;


    @Generated(hash = 683167796)
    public ReportEntity() {
    }


    @Generated(hash = 1845742658)
    public ReportEntity(Long id, @NotNull String appName, @NotNull String userID,
            @NotNull String appVersion, @NotNull String installedOn,
            @NotNull String remoteAddress, @NotNull String remoteHex,
            @NotNull String remoteHost, @NotNull String localAddress,
            @NotNull String localHex, @NotNull String servicePoint,
            @NotNull String payloadProtocol, @NotNull String transportProtocol,
            @NotNull String lastSeen) {
        this.id = id;
        this.appName = appName;
        this.userID = userID;
        this.appVersion = appVersion;
        this.installedOn = installedOn;
        this.remoteAddress = remoteAddress;
        this.remoteHex = remoteHex;
        this.remoteHost = remoteHost;
        this.localAddress = localAddress;
        this.localHex = localHex;
        this.servicePoint = servicePoint;
        this.payloadProtocol = payloadProtocol;
        this.transportProtocol = transportProtocol;
        this.lastSeen = lastSeen;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    public String getAppName() {
        return this.appName;
    }

    public void setAppName(@NotNull String appName) {
        this.appName = appName;
    }

    public String getUserID() {
        return this.userID;
    }

    public void setUserID(@NotNull String userID) {
        this.userID = userID;
    }

    public String getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getInstalledOn() {
        return this.installedOn;
    }

    public void setInstalledOn(String installedOn) {
        this.installedOn = installedOn;
    }

    public String getRemoteAddress() {
        return this.remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRemoteHex() {
        return this.remoteHex;
    }

    public void setRemoteHex(String remoteHex) {
        this.remoteHex = remoteHex;
    }

    public String getRemoteHost() {
        return this.remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getLocalAddress() {
        return this.localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getLocalHex() {
        return this.localHex;
    }

    public void setLocalHex(String localHex) {
        this.localHex = localHex;
    }

    public String getServicePoint() {
        return this.servicePoint;
    }

    public void setServicePoint(String servicePoint) {
        this.servicePoint = servicePoint;
    }

    public String getPayloadProtocol() {
        return this.payloadProtocol;
    }

    public void setPayloadProtocol(String payloadProtocol) {
        this.payloadProtocol = payloadProtocol;
    }

    public String getTransportProtocol() {
        return this.transportProtocol;
    }

    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public String getLastSeen() {
        return this.lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String toString(){
        return  "ID: " + id + "" +
                "\nAppname: "  + appName +
                "\nUID: " + userID +
                "\nAppVersion: " + appVersion +
                "\nInstalled On: " + installedOn +
                "\nRemote Address: " + remoteAddress +
                "\nRemote Hex: " + remoteHex +
                "\nRemote Host: " + remoteHost +
                "\nLocal Address: " + localAddress +
                "\nLocal Hex: " + localHex +
                "\nService Port: " + servicePoint +
                "\nPayload Protocol: " + payloadProtocol +
                "\nTransport Protocol: " + transportProtocol +
                "\nLast Seen: " + lastSeen;
    }

}
