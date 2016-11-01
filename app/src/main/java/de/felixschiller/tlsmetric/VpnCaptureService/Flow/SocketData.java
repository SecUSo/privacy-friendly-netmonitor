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

package de.felixschiller.tlsmetric.VpnCaptureService.Flow;

import java.sql.Timestamp;

/**
* Interface for protocol flow information (TCP/UCP)
*/

public abstract class SocketData {

    public int offset;
    public boolean isOpen;
    public boolean isGarbage;
    private int mVersion;
    private byte[] mSrcAdd;
    private byte[] mDstAdd;
    private int mSrcPort;
    private int mDstPort;
    private Timestamp mTime;
    private Transport mTrans;

    public SocketData(byte[] srcAdd, byte[] dstAdd, int srcPort, int dstPort, Timestamp time, int ipVersion){
        mVersion = ipVersion;
        mSrcAdd = srcAdd;
        mDstAdd = dstAdd;
        mSrcPort = srcPort;
        mDstPort = dstPort;
        mTime = time;}

    public void touchTime(){
        mTime = new Timestamp(System.currentTimeMillis());
    }

    //Getter and Setter
    public int getVersion() { return mVersion; }
    public void setVersion(int version) { this.mVersion = version; }

    public byte[] getSrcAdd() {return mSrcAdd;}
    public void setSrcAdd(byte[] srcAdd) {this.mSrcAdd = srcAdd;}

    public byte[] getDstAdd() {
        return mDstAdd;
    }
    public void setDstAdd(byte[] mSstAdd) {
        this.mDstAdd = mSstAdd;
    }

    public int getSrcPort() {
        return mSrcPort;
    }
    public void setSrcPort(int srcPort) {
        this.mSrcPort = srcPort;
    }

    public int getDstPort() {
        return mDstPort;
    }
    public void setDstPort(int dstPort) {
        this.mDstPort = dstPort;
    }

    public Timestamp getTime(){ return mTime;}
    public void setTime(Timestamp time){mTime = time;}

    public int getIpVersion(){ return mVersion;}
    public void setIpVersion(int ipVersion){ mVersion = ipVersion;}

    public Transport getTransport(){return mTrans;}
    public void setTransport(Transport trans){mTrans = trans; }

    /**
     * Data class for socket pair connections
     */
    public enum Transport{
        TCP,
        UDP
    }
}