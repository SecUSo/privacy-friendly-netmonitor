/*
    Privacy Friendly Net Monitor (Net Monitor)
    - Copyright (2015 - 2017) Felix Tsala Schiller

    ###################################################################

    This file is part of Net Monitor.

    Net Monitor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Net Monitor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Net Monitor.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von Net Monitor.

    Net Monitor ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Net Monitor wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.

    ###################################################################

    This app has been created in affiliation with SecUSo-Department of Technische Universität
    Darmstadt.

    The design is based on the Privacy Friendly Example App template by Karola Marky, Christopher
    Beckmann and Markus Hau (https://github.com/SecUSo/privacy-friendly-app-example).

    Privacy Friendly Net Monitor is based on TLSMetric by Felix Tsala Schiller
    https://bitbucket.org/schillef/tlsmetric/overview.

 */
package org.secuso.privacyfriendlynetmonitor.DatabaseUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by m4rc0 on 08.11.2017.
 * Handling one entity of a report.
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
    private  String timeStamp;
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
    private  String servicePort;
    @NotNull
    private  String payloadProtocol;
    @NotNull
    private  String transportProtocol;
    @NotNull
    private String localPort;
    @NotNull
    private String connectionInfo;
    @Generated(hash = 15093572)
    public ReportEntity(Long id, @NotNull String appName, @NotNull String userID,
            @NotNull String timeStamp, @NotNull String remoteAddress, @NotNull String remoteHex,
            @NotNull String remoteHost, @NotNull String localAddress, @NotNull String localHex,
            @NotNull String servicePort, @NotNull String payloadProtocol,
            @NotNull String transportProtocol, @NotNull String localPort,
            @NotNull String connectionInfo) {
        this.id = id;
        this.appName = appName;
        this.userID = userID;
        this.timeStamp = timeStamp;
        this.remoteAddress = remoteAddress;
        this.remoteHex = remoteHex;
        this.remoteHost = remoteHost;
        this.localAddress = localAddress;
        this.localHex = localHex;
        this.servicePort = servicePort;
        this.payloadProtocol = payloadProtocol;
        this.transportProtocol = transportProtocol;
        this.localPort = localPort;
        this.connectionInfo = connectionInfo;
    }
    @Generated(hash = 683167796)
    public ReportEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAppName() {
        return this.appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public String getUserID() {
        return this.userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getTimeStamp() {
        return this.timeStamp;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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
    public String getServicePort() {
        return this.servicePort;
    }
    public void setServicePort(String servicePort) {
        this.servicePort = servicePort;
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
    public String getLocalPort() {
        return this.localPort;
    }
    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }
    public String getConnectionInfo() {
        return this.connectionInfo;
    }
    public void setConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public String toString() {
        return "ReportEntity{" +
                "id=" + id +
                ", appName='" + appName + '\'' +
                ", userID='" + userID + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", remoteHex='" + remoteHex + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                ", localAddress='" + localAddress + '\'' +
                ", localHex='" + localHex + '\'' +
                ", servicePort='" + servicePort + '\'' +
                ", payloadProtocol='" + payloadProtocol + '\'' +
                ", transportProtocol='" + transportProtocol + '\'' +
                ", localPort='" + localPort + '\'' +
                ", connectionInfo='" + connectionInfo + '\'' +
                '}';
    }
    
    public String toStringWithoutTimestamp() {
        return "ReportEntity{" +
                "id=" + id +
                ", appName='" + appName + '\'' +
                ", userID='" + userID + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", remoteHex='" + remoteHex + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                ", localAddress='" + localAddress + '\'' +
                ", localHex='" + localHex + '\'' +
                ", servicePort='" + servicePort + '\'' +
                ", payloadProtocol='" + payloadProtocol + '\'' +
                ", transportProtocol='" + transportProtocol + '\'' +
                ", localPort='" + localPort + '\'' +
                '}';
    }
}
