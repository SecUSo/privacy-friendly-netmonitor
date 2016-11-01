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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import de.felixschiller.tlsmetric.Assistant.Const;
import de.felixschiller.tlsmetric.VpnCaptureService.Flow.SocketData;
import de.felixschiller.tlsmetric.VpnCaptureService.Flow.TcpFlow;

/**
 * Generates IP and TCP/UDP packets from payload (or none) and a given flow element.
 *
 */
public class PacketGenerator {

    /* Protocol Byte information below:
     * #############################################################################################
     * ##  IP4
     * #############################################################################################
     * Byte : Information
     * 1    : Version(4Bit) + Header Length(4Bit)
     * 2    : TOS
     * 3-4  : TotalLength
     * 5-6  : Identification
     * 7-8  : Flags(3 Bit: reserved - DF: Don't Fragment - MF: More Fragments+(13 Bit)FragmentOffset
     * 9    : TTL
     * 10   : Carrying protocol(17(0x11) = UDP, 6(0x06) = TCP)
     * 11-12: HeaderChecksum
     * 13-16: SrcAddress
     * 17-20: DstAddress
     * 21-40: Options, See RFC 791 - https://tools.ietf.org/html/rfc791 -- ignored.
     *
     * #############################################################################################
     * ##   IP6
     * #############################################################################################
     *
     * Not yet implemented.
     *
     * #############################################################################################
     * ##   UDP
     * #############################################################################################
     * Byte : Information
     * 1-2  : Source Port
     * 3-4  : Destination Port
     * 5-6  : Length of UDP Header(8) + Payload in Bytes
     * 7-8  : Checksum (or Zero) -- Note needed internally and hence ignored.
     *
     * #############################################################################################
     * ##   TCP
     * #############################################################################################
     * Byte : Information
     * 1-2  : Source Port
     * 3-4  : Destination Port
     * 5-8  : Sequence Number
     * 9-12 : Acknowledge Number
     * 13   : Header Length (Bit 0-3 * 4 Bytes, like IP), (Bit 4-7 reserved and nonce Bit)
     * 14   : Flags (CWR, ECN, URG, ACK, PSH, RST, SYN, FIN)
     * 15-16: Window Size (value * scaling factor 256)
     * 17-18: Checksum
     * 19-20: Urgent Pointer (offset)
     * 21-40: Options (if defined in header length - ignored)
     * #############################################################################################
    */
    private static final byte[] sIp4Dummy = hexStringToByteArray("45000086001100003d1100001111111122222222");
    private static final byte[] sIp6Dummy = hexStringToByteArray("");
    private static final byte[] sTcpDummy = hexStringToByteArray("111122221111111122222222501005ac00000000");
    private static final byte[] sUdpDummy = hexStringToByteArray("111122220072fc42");

    //Generates packets based on stored connection data.
    public static byte[] generatePacket(SocketData data, byte[] b, byte flag){
        if(data.getIpVersion() == 4){
            if(data.getTransport() == SocketData.Transport.TCP){
                //Generate a ip(tcp(payload)) packet with connection data and checksum
                int length = sIp4Dummy.length + sTcpDummy.length + b.length;
                ByteBuffer bb = ByteBuffer.allocate(length);
                byte[] ipPayload = forgeTCP((TcpFlow)data, b, flag);
                bb.put(forgeIp4(data, ipPayload));
                return bb.array();
            }
            else if(data.getTransport() == SocketData.Transport.UDP){
                //Generate a ip(udp(payload)) packet with connection data and checksum
                int length = sIp4Dummy.length + sUdpDummy.length + b.length;
                ByteBuffer bb = ByteBuffer.allocate(length);
                byte[] ipPayload = forgeUDP(data, b);
                bb.put(forgeIp4(data, ipPayload));
                return bb.array();
            }
        }

        //TODO: IPv6 Implementation
        else if(data.getIpVersion() == 6) {
            if(data.getTransport() == SocketData.Transport.TCP){
                //Generate a ip(tcp(payload)) packet with connection data and checksum
                int length = sIp6Dummy.length + sTcpDummy.length + b.length;
                ByteBuffer bb = ByteBuffer.allocate(length);
                bb.put(sIp6Dummy);
                bb.put(sTcpDummy);
                bb.put(b);

                return bb.array();
            }
            else if(data.getTransport() == SocketData.Transport.UDP){
                //Generate a ip(udp(payload)) packet with connection data and checksum
                int length = sIp6Dummy.length + sUdpDummy.length + b.length;
                ByteBuffer bb = ByteBuffer.allocate(length);
                byte[] ipPayload = forgeUDP(data, b);
                bb.put(forgeIp4(data, ipPayload));
                return bb.array();
            }
        }
        return b;
    }

    //forge an IPv4 header
    public static byte[] forgeIp4(SocketData data, byte[] payload){
        int length =  sIp4Dummy.length + payload.length;
        //ByteBuffers for assembling and int-conversion.
        ByteBuffer bb = ByteBuffer.allocate(length);
        ByteBuffer bint = ByteBuffer.allocate(4);
        bb.put(sIp4Dummy);
        bb.put(payload);
        //fill packet length
        bb.position(2);
        bint.position(0);
        bint.putInt(length);
        bb.put(bint.array(), 2, 2);
        //Identification Field needed?
        //fill protocol
        bb.position(9);
        if(data.getTransport() == SocketData.Transport.UDP){
            bb.put((byte)17);
        } else{
            bb.put((byte)6);
        }
        //Add source and destination address
        bb.position(12);
        bb.put(data.getDstAdd());
        bb.put(data.getSrcAdd());

        //Last but not least: generate checksum
        bb.position(0);
        byte[] v4header = new byte[20];
        bb.get(v4header);
        byte[] cs = longToFourBytes(computeChecksum(v4header));
        bb.position(10);
        bb.put(cs, 2, 2);
        return bb.array();
    }

    //forge an IPv4 header
    public static byte[] forgeIp6(SocketData data, byte[] payload){
        //TODO: implement
        return null;
    }

    //forge an TCP header
    public static byte[] forgeTCP(TcpFlow data, byte[] payload, Byte flag){
        int length =  sTcpDummy.length + payload.length;
        //ByteBuffers for assembling and int-conversion.
        ByteBuffer bb = ByteBuffer.allocate(length);
        ByteBuffer bint = ByteBuffer.allocate(4);
        bb.put(sTcpDummy);
        bb.put(payload);
        bb.position(0);

        //fill source and destination ports
        bint.putInt(data.getDstPort());
        bb.put(bint.array(), 2, 2);
        bint.position(0);
        bint.putInt(data.getSrcPort());
        bb.put(bint.array(), 2, 2);

        //Manage AckNumber
        if(!data.ackQueue.isEmpty()){
            data.ackNr = data.ackQueue.poll();
        }
        if(!data.seqQueue.isEmpty()){
            data.seqNr = data.seqQueue.poll();
        }
        bb.put(data.seqNr);
        //insert ack number (calculated in send method)
        bb.put(data.ackNr);

        //increment seq Number if need be and add to queue
        long inc = fourBytesToLong(tcpIncrementer(data.ackNr, payload.length));
        if(!(inc == fourBytesToLong(data.seqNr))){
         data.seqQueue.add(longToFourBytes(inc));
        }

        //set the flag code
        bb.position(13);
        bb.put(flag);

        //Checksum generation
        byte[] pseudoHeader = generatePseudoHeader(data, bb.array());
        byte[] cs = longToFourBytes(computeChecksum(pseudoHeader));
        bb.position(16);
            bb.put(cs, 2, 2);
        return bb.array();
    }


    //Forge an udp packet
    public static byte[] forgeUDP(SocketData data, byte[] payload){
        int length =  sUdpDummy.length + payload.length;
        //ByteBuffers for assembling and int-conversion.
        ByteBuffer bb = ByteBuffer.allocate(length);
        ByteBuffer bint = ByteBuffer.allocate(4);
        bb.put(sUdpDummy);
        bb.put(payload);
        bb.position(0);
        //fill source and destination ports
        bint.putInt(data.getDstPort());
        bb.put(bint.array(), 2, 2);
        bint.position(0);
        bint.putInt(data.getSrcPort());
        bb.put(bint.array(), 2, 2);
        //fill packet length
        bint.position(0);
        bint.putInt(length);
        bb.put(bint.array(), 2, 2);
        //fill checksum with 0 no Checksum used
        bint.position(0);
        bint.putInt(0);
        bb.put(bint.array(), 2, 2);
        return bb.array();
    }

    //String to java-readable byte array.
    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    //Extract TCP flags
    public static void handleFlags(TcpFlow data, Header header) {
        data.flags = intToTwoBytes((int) header.getValue("code"));
        data.fin = ((data.flags[1] & (byte) 0x01) != (byte) 0x00);
        data.syn = ((data.flags[1] & (byte) 0x02) != (byte) 0x00);
        data.rst = ((data.flags[1] & (byte) 0x04) != (byte) 0x00);
    }

    //Extract Flow an Seq numbers of initial flow
    public static void initFlow(TcpFlow data, Header header){
        //generate initial ACK and SEQ NR
        if (data.seqNr == null) {
            if ((long) header.getValue("ack") != 0){
                data.seqNr = longToFourBytes((long) header.getValue("ack"));
            } else {
                data.seqNr = longToFourBytes((long) header.getValue("seq"));
            }
        }
        if (data.ackNr == null) {
            data.ackNr = longToFourBytes((long) header.getValue("seq"));
        }
        handleFlags(data, header);
    }


     //TCP flow control and packet forging logic methods for transmitting packages.
    public static void handleFlowAtSend(TcpFlow data, Header header, int payloadLen){
        if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Handle Flow of channel id: " + data.getSrcPort()
                + " payload length: " + payloadLen);

        //generate initial ACK and SEQ NR
        if (data.seqNr == null) {
            if ((long) header.getValue("ack") != 0){
                data.seqNr = longToFourBytes((long) header.getValue("ack"));
            } else {
                data.seqNr = longToFourBytes((long) header.getValue("seq"));
            }
        }
        if (data.ackNr == null) {
            data.ackNr = longToFourBytes((long) header.getValue("seq"));
        }


        //Add next expected acknowledgement number to queue
        long inc = fourBytesToLong(tcpIncrementer(longToFourBytes((long)header.getValue("seq")), payloadLen));
        if(!(inc == fourBytesToLong(data.ackNr))){
            data.ackQueue.add(longToFourBytes(inc));
        }

        handleFlags(data, header);
    }

    //TCP flow control and packet forging logic methods called after receiving packages.
    public static void handleFlowAtRecieve(TcpFlow data, Header header, int payloadLen){
        if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Handle receiving Flow, payload: " + payloadLen + " Bytes.");

        long inc = fourBytesToLong(tcpIncrementer(data.seqNr, payloadLen));
        if(!(inc == fourBytesToLong(data.seqNr))){
            data.seqQueue.add(tcpIncrementer(longToFourBytes((long) header.getValue("seq")), payloadLen));
        }
    }

    //Establish a connection by forging a SYN-ACK control package
    public static byte[] forgeHandshake(TcpFlow flow){

        //Clear ack and seq queues
        flow.ackQueue = new LinkedList<>();
        flow.seqQueue = new LinkedList<>();

        //Increment Ack + 1 and generate Answer
        flow.ackNr = tcpIncrementer(flow.ackNr, 1);
        byte flag = (byte)0x12;
        byte[] synAck = generatePacket(flow, new byte[]{}, flag);
        //create syn/ack flag
        //Increment Seq + 1 and set Ignore Next Packet
        flow.seqNr = tcpIncrementer(flow.seqNr, 1);
        //Set the flow to handshake completed
        flow.isOpen = true;
        return synAck;
    }

    //Increment Ack = SYN + 1; SYN = ACK and generate ACK control packet
    public static byte[] forgeBreakdown(TcpFlow flow) {
            //Add at head of ACK queue
            if (flow.ackQueue.isEmpty()) {
                flow.ackQueue.add(tcpIncrementer(flow.seqNr, 1));
            }else {
                Queue<byte[]> newQ = new LinkedList<>();
                newQ.add(tcpIncrementer(flow.seqNr, 1));
                while (!flow.ackQueue.isEmpty()) {
                    newQ.add(flow.ackQueue.poll());
                }
                flow.ackQueue = newQ;
            }

        //Set flow to closed and destroyed
        flow.isOpen = false;
        flow.isGarbage = true;
        flow.isBreakdown = true;

        flow.touchTime();
        byte[] ack = generatePacket(flow, new byte[]{}, (byte)0x10);
        return ack;
    }

    //Generate control packet with FIN flag
    public static byte[] forgeFin(TcpFlow flow){
            if(Const.IS_DEBUG)Log.d(Const.LOG_TAG,"Sending ACK-FIN");
            return generatePacket(flow, new byte[]{}, (byte)0x11);
    }

    //Generate control packet with RST flag
    public static byte[] forgeReset(TcpFlow flow){
        if(Const.IS_DEBUG)Log.d(Const.LOG_TAG,"Sending ACK-FIN");

        flow.isOpen = false;
        flow.isGarbage = true;
        flow.isBreakdown = false;

        return generatePacket(flow, new byte[]{}, (byte)0x04);
    }

    //Increments a seq or ack number by the given offset, normally the packet length
    public static byte[] tcpIncrementer(byte[] number, int offset) {
        //ByteBuffer to convert the Int-s
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.position(4);
        bb.put(number);
        bb.position(0);
        long num = bb.getLong();
        if (num + offset > Long.MAX_VALUE){
            num = (num + offset)%Long.MAX_VALUE;
            return longToFourBytes(num);
        } else{
            return longToFourBytes(num+offset);
        }
    }

    /*
     * Calculate the Internet Checksum of a buffer (RFC 1071 - http://www.faqs.org/rfcs/rfc1071.html)
     * Algorithm is
     * 1) apply a 16-bit 1's complement sum over all octets (adjacent 8-bit pairs [A,B], final odd length is [A,0])
     * 2) apply 1's complement to this final sum
     *
     * Notes:
     * 1's complement is bitwise NOT of positive value.
     * Ensure that any carry bits are added back to avoid off-by-one errors
     */
    public static long computeChecksum(byte[] buf) {
        int length = buf.length;
        int i = 0;

        long sum = 0;
        long data;

        // Handle all pairs
        while (length > 1) {
            data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
            sum += data;
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }

            i += 2;
            length -= 2;
        }

        // Handle remaining byte in odd length buffers
        if (length > 0) {
            sum += (buf[i] << 8 & 0xFF00);
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }

        // Final 1's complement value correction to 16-bits
        sum = ~sum;
        sum = sum & 0xFFFF;
        return sum;
    }

    //Generates the pseudo header, used for TCP checksum
    public static byte[] generatePseudoHeader(TcpFlow data, byte[] b){
        ByteBuffer bb = ByteBuffer.allocate(12+b.length);
        bb.put(data.getDstAdd());
        bb.put(data.getSrcAdd());
        bb.put((byte)0x00);
        bb.put((byte) 0x06);
        bb.put(intToTwoBytes(b.length));
        bb.put(b);
        return bb.array();
    }

    //Convert a Java long to a four byte array
    public static byte[] longToFourBytes(long l){
        ByteBuffer bb = ByteBuffer.allocate(8);
        byte[] b = new byte[4];
        bb.putLong(l);
        bb.position(4);
        bb.get(b);
        return b;

    }

    //Convert a Java int to a two byte array
    public static byte[] intToTwoBytes(int i){
        ByteBuffer bb = ByteBuffer.allocate(4);
        byte[] b = new byte[2];
        bb.putInt(i);
        bb.position(2);
        bb.get(b);
        return b;
    }

    //Convert four bytes to a Java Long
    public static long fourBytesToLong(byte[] b){
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.position(4);
        bb.put(b);
        bb.position(0);
        return bb.getLong();

    }

}

