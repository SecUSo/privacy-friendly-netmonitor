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

package de.felixschiller.tlsmetric.VpnCaptureService;

import android.util.Log;

import com.voytechs.jnetstream.codec.Header;
import com.voytechs.jnetstream.codec.Packet;
import com.voytechs.jnetstream.primitive.address.Address;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;

import de.felixschiller.tlsmetric.Assistant.Const;
import de.felixschiller.tlsmetric.VpnCaptureService.Flow.SocketData;
import de.felixschiller.tlsmetric.VpnCaptureService.Flow.TcpFlow;
import de.felixschiller.tlsmetric.VpnCaptureService.Flow.UdpFlow;

/**
 * Handler for socket and connection management.
 */
public class ConnectionHandler {


    /*
     * Extracts the necessary ISO/OSI layer3/layer4 header data for the connection flow. Existence
     * of an IP-Header has to be already confirmed.
     */
    public static SocketData extractFlowData(Packet pkt) {
        Header ipHeader;
        Header transportHeader;
        int version;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SocketData flow;

        //Read IP and transport Header
        if (pkt.hasHeader("IPv4")) {
            ipHeader = pkt.getHeader("IPv4");
            version = 4;
        } else if (pkt.hasHeader("IPv6")){
            ipHeader = pkt.getHeader("IPv6");
            version = 6;
        } else {
            ipHeader = null;
            version = -1;
        }

        if (pkt.hasHeader("TCP") && ipHeader != null) {
            transportHeader = pkt.getHeader("TCP");
            flow = new TcpFlow(new byte[]{}, new byte[]{},0, 0, timestamp, version);
            flow.offset = (int) ipHeader.getValue("hlen") * 4 + (int)transportHeader.getValue("hlen") * 4;
        }else if (pkt.hasHeader("UDP") && ipHeader != null ) {
            transportHeader = pkt.getHeader("UDP");
            flow = new UdpFlow(new byte[]{}, new byte[]{},0, 0, timestamp, version);
            flow.offset = (int) ipHeader.getValue("hlen") * 4 + 8;
        } else {
            transportHeader = null;
            flow = null;
        }

        if(flow != null) {
            //Get ports and addresses from header
            Address address = (Address)ipHeader.getValue("daddr");
            flow.setDstAdd(address.toByteArray());
            address = (Address)ipHeader.getValue("saddr");
            flow.setSrcAdd(address.toByteArray());
            flow.setDstPort((int) transportHeader.getValue("dport"));
            flow.setSrcPort((int) transportHeader.getValue("sport"));

        }
        return flow;
    }


    // Get header length to payload data
    public static int getHeaderOffset(Packet pkt){
        Header ipHeader;
        Header transportHeader;

        //Read IP and transport Header
        if (pkt.hasHeader("IPv4")) {
            ipHeader = pkt.getHeader("IPv4");
        } else if (pkt.hasHeader("IPv6")){
            ipHeader = pkt.getHeader("IPv6");
        } else {
            ipHeader = null;
        }

        if (pkt.hasHeader("TCP") && ipHeader != null) {
            transportHeader = pkt.getHeader("TCP");
            return (int) ipHeader.getValue("hlen") * 4 + (int)transportHeader.getValue("hlen") * 4;
        }else if (pkt.hasHeader("UDP") && ipHeader != null ) {
            return (int) ipHeader.getValue("hlen") * 4 + 8;
        } else {
            return -1;
        }
    }



    //Kills all channels the selector holds.
    public static void killAll() throws IOException {
        if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Closing ALL channels.");
        Set<SelectionKey> allKeys = VpnCaptureService.mSelector.keys();
        Iterator<SelectionKey> keyIterator = allKeys.iterator();
        SelectionKey key;
        while (keyIterator.hasNext()) {
            key = keyIterator.next();
            key.channel().close();
            key.cancel();
        }
    }

    // Extract TCP header flags.
    public static byte[] handleFlags(TcpFlow flow) throws IOException {

        //Detect SYN Flag and initiate Handshake if present
        if (flow.syn && !flow.isOpen && !flow.isGarbage) {
            if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "SYN Flag detected, initiating Handshake.");
            return PacketGenerator.forgeHandshake(flow);

        }

        //Detect Rst flag and Handle
        if (flow.rst) {
            if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "RST Flag detected, initiating closing sequence.");
            //TODO: sent fin ack packet
            return null;
        }

        //Detect Rst flag and close/unregister from Selector
        if (flow.fin) {
            if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "FIN Flag detected, initiating closing sequence.");
            return PacketGenerator.forgeBreakdown(flow);
        }
        return null;
    }
}
