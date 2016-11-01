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

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.voytechs.jnetstream.codec.Decoder;
import com.voytechs.jnetstream.codec.Packet;
import com.voytechs.jnetstream.io.EOPacketStream;
import com.voytechs.jnetstream.io.QueuePacketInputStream;
import com.voytechs.jnetstream.io.StreamFormatException;
import com.voytechs.jnetstream.npl.SyntaxError;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import de.felixschiller.tlsmetric.Assistant.Const;
import de.felixschiller.tlsmetric.R;
import de.felixschiller.tlsmetric.VpnCaptureService.Flow.SocketData;
import de.felixschiller.tlsmetric.VpnCaptureService.Flow.TcpFlow;
import de.felixschiller.tlsmetric.VpnCaptureService.Flow.UdpFlow;

/**
 * A VPN packet capturing service. All Packets get repacket and sent directly to the target hosts
 * by datagram and socket channels. Supports UDP and TCP protocols.
 *
*/
public class VpnCaptureService extends VpnService {

    // Keys for all channels are the source ports (client ports) of the outgoing connection.
    public static Selector mSelector;
    public static Queue<QueuePacket> mSendQueue = new LinkedList<>();

    //channel List to combat racing conditions
    public static HashSet<Integer> mChannels = new HashSet<>();
    public static QueuePacketInputStream mClone;

    //Creator for VPN interface
    Builder builder = new Builder();

    //Thread
    private Thread mThread;
    private ParcelFileDescriptor mInterface;

    // Packet stream decoder JnetStream:
    private QueuePacketInputStream mPin;
    private Decoder mDecoder;
    private FileInputStream mIn;
    private FileOutputStream mOut;

    //Kickstart!
    public static void start(Context context) {
        Intent intent = new Intent(context, VpnCaptureService.class);
        context.startService(intent);
    }

    //Sends the packet out, if there's paylaod
    public static boolean sendPacket(byte[] b, Packet pkt, SelectionKey key) throws IOException {
        SocketData data = (SocketData) key.attachment();
        data.offset = ConnectionHandler.getHeaderOffset(pkt);
        int payload = b.length - data.offset;
        if (payload > 0) {
            ByteBuffer bb = ByteBuffer.allocate(payload);
            bb.put(b, data.offset, payload);
            bb.position(0);

            //Send TCP
            if (data.getTransport() == SocketData.Transport.TCP) {
                SocketChannel sChan = (SocketChannel) key.channel();
                if (!sChan.isConnected()) {
                    if (Const.IS_DEBUG)
                        Log.d(Const.LOG_TAG, "Channel not yet connected, add packet to send queue");
                    sChan.finishConnect();
                    mSendQueue.add(new QueuePacket(key, b, pkt));
                } else {
                    try {
                        int sent = sChan.write(bb);
                        if (Const.IS_DEBUG)
                            Log.d(Const.LOG_TAG, "Channel ID: " + data.getSrcPort() + ", sent " + sent + " of " + payload + " bytes.");
                        PacketGenerator.handleFlowAtSend((TcpFlow) data, pkt.getHeader("TCP"), payload);
                        return true;
                    } catch (SocketException e) {
                        Log.e(Const.LOG_TAG, "SocketError - Reset connection ID:" + data.getSrcPort());
                        PacketGenerator.forgeReset((TcpFlow) data);
                        return false;
                    }
                }
                //Send UDP
            } else if (data.getTransport() == SocketData.Transport.UDP) {
                DatagramChannel dChan = (DatagramChannel) key.channel();
                if (dChan.isConnected()) {
                    int sent = dChan.write(bb);
                    if (Const.IS_DEBUG)
                        Log.d(Const.LOG_TAG, "Channel ID: " + data.getSrcPort() + ", sent " + sent + " of " + payload + " bytes.");
                    return true;
                } else {
                    Log.d(Const.LOG_TAG, "Could not write to channel ID: " + data.getSrcPort() + ", adding packet to queue");
                    return false;
                }
            } else {
                Log.d(Const.LOG_TAG, "Packet not sent. Payload: " + payload);
                return false;
            }
        } else {
            Log.d(Const.LOG_TAG, "Packet not sent. No Payload.");
            return true;
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
        }


        //Init selector and jnet package streams
        try {
            mSelector = Selector.open();

            mPin = new QueuePacketInputStream();
            mClone = new QueuePacketInputStream();
            mDecoder = new Decoder(mPin);
        } catch (IOException | SyntaxError | StreamFormatException | EOPacketStream e) {
            e.printStackTrace();
        }

        // Start a new session by creating a new thread.
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Configure the TUN interface.
                    mInterface = builder.setSession("VPNBypassService")
                            //TODO: add Ipv6 and MTU
                            .addAddress("10.0.2.1", 32)
                            .addRoute("0.0.0.0", 1)
                            .addRoute("128.0.0.0", 1)
                            .establish();
                    //Get FileStream
                    mIn = new FileInputStream(mInterface.getFileDescriptor());
                    mOut = new FileOutputStream(mInterface.getFileDescriptor());

                    while (!Thread.currentThread().isInterrupted() &&
                            mInterface.getFileDescriptor() != null &&
                            mInterface.getFileDescriptor().valid()) {
                        try {
                            // Initialize byte array with 65535 bytes = maxsize of an IP Packet
                            byte[] b = new byte[65535];

                            // If data is available, read it and process it for the designated channel
                            int available = mIn.read(b);
                            if (available > 0) {
                                if (Const.IS_DEBUG)
                                    Log.d(Const.LOG_TAG, available + " available at TUN interface.");
                                transmit(b, available);

                            } else {
                                //if (Const.IS_DEBUG)
                                //    Log.d(Const.LOG_TAG, "no data available at TUN interface.");
                            }

                            //read from sockets where data is available and process it for write back to TUN.
                            receive();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //Kill channels in with destroyed-flag afgter timeout
                        garbageChannels();

                        //TODO test Value
                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    // Catch any exception
                    e.printStackTrace();
                } finally {
                    try {
                        ConnectionHandler.killAll();
                        mSelector.close();
                        if (mInterface != null) {
                            mInterface.close();
                            mInterface = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }, "VpnBypassRunnable");

        //start the service
        mThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (Const.IS_DEBUG) Log.d(getString(R.string.app_name), "Destroy VpnCaptureService.");
        if (mThread != null) {
            mThread.interrupt();
        }
        super.onDestroy();
    }

    /*
     * Receives a packet from internal TUN interface. Strips the packet of the payload and sends
     * it to the designated server. For UDP payload a DatagramChannel, for tcp a SocketChannel is
     * opened and addressed by the source port of the original connection (TUN interface side).
     * Closing of channels is handles by the packet recieving method.
     *
     */
    private void transmit(byte[] b, int available) throws IOException, StreamFormatException, SyntaxError {

        //Allocate buffer and read from TUN interface and dump it
        ByteBuffer bb = ByteBuffer.allocate(available);
        bb.put(b, 0, available);
        b = bb.array();

        //Dump the packet and process it, if ip header is identified.
        Packet pkt = dumpPacket(b);
        if (pkt != null) {

            //Extract connection information
            SocketData data = ConnectionHandler.extractFlowData(pkt);

            //Process outgoing send
            ManageSending(data, b, pkt);
        }
    }

    /*
     * Read from active sockets and channels and generate the feedback ip-packet
     */
    private void receive() throws IOException, StreamFormatException, SyntaxError {
        byte[] b = ManageReceiving();


        //If there is any usable data, write it back to TUN interface
        if (b != null) {
            mOut.write(b);
        }
    }

    /*
     * Creates a IP packet dump of the given byte array and adds it to a separated clone buffer for
     * later processing.
     */
    private Packet dumpPacket(byte[] b) throws IOException, StreamFormatException, SyntaxError {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        //Add b to input buffer and try to determine decode the ip packet
        mPin.addBuffer(b, "IPv4", timestamp);
        Packet pkt = mDecoder.nextPacket();
        if (pkt.hasHeader("IPv4")) {
            if (Const.IS_DEBUG)
                Log.d(Const.LOG_TAG, "Dumped " + b.length + " Bytes: " + pkt.getSummary());
            mClone.addBuffer(b, "IPv4", timestamp);
            //if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Dumped " + b.length + " Bytes: " + pkt.getSummary() + ": " + ToolBox.printExportHexString(b));
            return pkt;
        } else {
            mPin.addBuffer(b, "IPv6", timestamp);
            pkt = mDecoder.nextPacket();
            if (pkt.hasHeader("IPv6")) {
                mClone.addBuffer(b, "IPv6", timestamp);
                if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, pkt.getSummary());
                return pkt;
            } else {
                Log.e(Const.LOG_TAG, "Could not identify packet.");
                return null;
            }
        }
    }

    /*
     * Sends the packet over an existing connection or registers a new channel to the selector.
     */
    public void ManageSending(SocketData data, byte[] b, Packet pkt) throws IOException {

        //High timeout variable to combat racing conditions
        mSelector.selectNow();
        Set allKey = VpnCaptureService.mSelector.selectedKeys();
        Iterator<SelectionKey> keyIterator = allKey.iterator();
        SelectionKey key;

        //If send queue is not empty, send queue first.
        if (!mSendQueue.isEmpty()) {
            QueuePacket qPkt = mSendQueue.poll();
            if (sendPacket(qPkt.b, qPkt.pkt, qPkt.key)) {
                if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Sending Packet from queue.");
            }
        }

        //When channel is existent, send the packet, but drop the existing flow information
        while (keyIterator.hasNext()) {
            key = keyIterator.next();
            SocketData attachedFlow = (SocketData) key.attachment();
            if (attachedFlow.getSrcPort() == data.getSrcPort()) {
                if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Channel " + attachedFlow.getSrcPort()
                        + ", isWritable:" + key.isWritable());

                //If it is a TCP connection, handle the flow information
                if (attachedFlow.getTransport() == SocketData.Transport.TCP) {

                    //Process TCP flags and generate/write back control packets
                    PacketGenerator.handleFlags((TcpFlow) attachedFlow, pkt.getHeader("TCP"));
                    byte[] controlPacket = ConnectionHandler.handleFlags((TcpFlow) attachedFlow);
                    if (controlPacket != null) {
                        try {
                            dumpPacket(controlPacket);
                        } catch (StreamFormatException | SyntaxError e) {
                            e.printStackTrace();
                        }
                        mOut.write(controlPacket);
                    }
                }

                //send
                sendPacket(b, pkt, key);
            }
        }

        // Create new Channel if no connection present and send it
        if (!mChannels.contains(data.getSrcPort())) {
            newConnection(data, b, pkt);
        }

    }

    /*
    * Creates a new Datagram- or SocketChannel based on extracted flow data.
    */
    private void newConnection(SocketData data, byte[] b, Packet pkt) throws IOException {
        SelectionKey key;

        if (data.getTransport() == SocketData.Transport.UDP) {
            key = registerChannel(data);
            if (key == null) {
                Log.e(Const.LOG_TAG, "Could not register Datagram Channel ID: " + data.getSrcPort());
            } else {
                sendPacket(b, pkt, key);
            }

            //Enhanced logic for TCP connection
        } else if (data.getTransport() == SocketData.Transport.TCP) {
            TcpFlow flow = (TcpFlow) data;
            flow.offset = ConnectionHandler.getHeaderOffset(pkt);
            PacketGenerator.initFlow(flow, pkt.getHeader("TCP"));

            //Open channel if SYN flag present
            if (flow.syn) {
                key = registerChannel(flow);
                mChannels.add(flow.getSrcPort());
                if (key == null) {
                    Log.e(Const.LOG_TAG, "Could not register Socket Channel ID: " + data.getSrcPort());
                } else {
                    sendPacket(b, pkt, key);
                }
            }

            //Process TCP flags and generate/write back control packets
            byte[] controlPacket = ConnectionHandler.handleFlags(flow);
            if (controlPacket != null) {
                try {
                    dumpPacket(controlPacket);
                } catch (StreamFormatException | SyntaxError e) {
                    e.printStackTrace();
                }
                mOut.write(controlPacket);
            }
        }
    }

    /*
    * Register, connect and protect the Channel by flow-data
    */
    public SelectionKey registerChannel(SocketData data) throws IOException {

        SelectionKey key = null;
        if(data.getTransport() == SocketData.Transport.TCP) {
            TcpFlow flow = (TcpFlow) data;
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE |SelectionKey.OP_CONNECT;
            key = socketChannel.register(mSelector, interestSet, data);
            if (!protect(socketChannel.socket())) {
                Log.e(Const.LOG_TAG, "Could not protect socket");
            } else {
                InetSocketAddress socksAdd = new InetSocketAddress(InetAddress.getByAddress(flow.getDstAdd()), flow.getDstPort());
                socketChannel.connect(socksAdd);
                if (Const.IS_DEBUG) {
                    Log.d(Const.LOG_TAG, "Connecting SocketChannel ID: " + data.getSrcPort() + " to: " + socksAdd.getAddress() + ":" + socksAdd.getPort());
                }
            }
        }
        else if(data.getTransport() == SocketData.Transport.UDP) {
            UdpFlow flow = (UdpFlow) data;
            DatagramChannel datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            int interestSet = SelectionKey.OP_READ |SelectionKey.OP_WRITE;
            key = datagramChannel.register(VpnCaptureService.mSelector, interestSet, data);
            if (!protect(datagramChannel.socket())) {
                Log.e(Const.LOG_TAG, "Could not protect socket");
            } else {
                datagramChannel.connect(new InetSocketAddress(InetAddress.getByAddress(flow.getDstAdd()), flow.getDstPort()));
                if (Const.IS_DEBUG) {
                    InetSocketAddress socksAdd = (InetSocketAddress) datagramChannel.socket().getRemoteSocketAddress();
                    Log.d(Const.LOG_TAG, "Connecting DatagramChannel ID: " + data.getSrcPort() + " to: " + socksAdd.getAddress().toString() + ":" + socksAdd.getPort());
                }
            }
        }
        return key;
    }
    /*
    * If there is readable data at the channels, a forged packet based on the flow data will be returned.
    */
    public byte[]  ManageReceiving() throws IOException, StreamFormatException, SyntaxError {

        mSelector.selectNow();
        Set<SelectionKey> keySet = VpnCaptureService.mSelector.selectedKeys();
        Iterator<SelectionKey> keyIterator = keySet.iterator();
        byte[] b;
        //Read bytes where channels have data available
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            SocketData data = (SocketData) key.attachment();
           // if(Const.IS_DEBUG)Log.d(Const.LOG_TAG,"Receiving channel " + data.getSrcPort()
             //       + " status: isReadable:" + key.isReadable());
            if(key.isReadable()) {
                ByteBuffer bb = ByteBuffer.allocate(65535);
                int read = 0;
                if (data.getTransport() == SocketData.Transport.TCP) {
                    TcpFlow flow = (TcpFlow) data;
                    SocketChannel sChan = (SocketChannel) key.channel();
                    if (sChan.isConnected()){
                        read = sChan.read(bb);
                    } else{
                        if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Channel ID: " + data.getSrcPort() + " not connected. Scheduled to cancel connection.");
                        data.isGarbage = true;
                        data.isOpen = false;
                    }

                } else if (data.getTransport() == SocketData.Transport.UDP) {
                    DatagramChannel sChan = (DatagramChannel) key.channel();
                    read = sChan.read(bb);
                }

                //Read the bytes to an array and generate a forged packet based on the flow data
                if (read > 0){
                    b = new byte[read];

                    bb.position(0);
                    bb.get(b);
                    b = PacketGenerator.generatePacket(data, b, (byte)0x10);

                    //Dump what has ben read
                    Packet pkt = dumpPacket(b);

                    //If TCP, write back flow information.
                    if(data.getTransport() == SocketData.Transport.TCP){
                        PacketGenerator.handleFlowAtRecieve((TcpFlow) data, pkt.getHeader("TCP"), read);
                    }
                    if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Channel ID: " + data.getSrcPort()
                            + ": " + read + " Bytes read. ");
                    return b;
                }
                else if (read == 0){
                    //if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "No bytes ready to read at channel ID: " + data.getSrcPort());
                }
                else if (read < 0){
                    if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Could not read from channel ID: " + data.getSrcPort());
                }
            }
            keyIterator.remove();

        }
        return null;
    }

    /*
    * Kills channels if expired or void
    */
    public void garbageChannels() throws IOException {
        // Timeout for garbage channels
        Timestamp time;
        Set<SelectionKey> allKeys = VpnCaptureService.mSelector.keys();
        Iterator<SelectionKey> keyIterator = allKeys.iterator();
        SelectionKey key;
        while (keyIterator.hasNext()) {
            key = keyIterator.next();
            SocketData data = (SocketData) key.attachment();
            if (data.isGarbage) {

                if (data.getTransport() == SocketData.Transport.TCP) {
                    time = new Timestamp(System.currentTimeMillis() - Const.CHANNEL_TIMEOUT_TCP);
                    TcpFlow flow = (TcpFlow) data;

                    //Connection breakdown (FIN)
                    if (flow.isBreakdown && time.after(data.getTime())) {
                        byte[] b = PacketGenerator.forgeFin(flow);
                        try {
                            dumpPacket(b);
                        } catch (StreamFormatException | SyntaxError e) {
                            e.printStackTrace();
                        }
                        mOut.write(b);

                        if (Const.IS_DEBUG)
                            Log.d(Const.LOG_TAG, "Connection Breakdown of Channel ID = " + data.getSrcPort());
                        key.channel().close();
                        key.cancel();
                        mChannels.remove(data.getSrcPort());
                    }

                    //Connection reset (RST)
                    if(!flow.isBreakdown){
                        Log.d(Const.LOG_TAG, "Connection Kill of Channel ID = " + data.getSrcPort());
                        key.channel().close();
                        key.cancel();
                        mChannels.remove(data.getSrcPort());
                    }


                } else if (data.getTransport() == SocketData.Transport.UDP) {
                    time = new Timestamp(System.currentTimeMillis() - Const.CHANNEL_TIMEOUT_UDP);

                    //Timeout Destroy
                    if (time.after(data.getTime())) {
                        if (Const.IS_DEBUG)
                            Log.d(Const.LOG_TAG, "Timeout closing UDP Channel ID = " + data.getSrcPort());
                        key.channel().close();
                        key.cancel();
                        mChannels.remove(data.getSrcPort());
                    }

                    //Close Destroy
                    if(!data.isOpen){
                        if (Const.IS_DEBUG)
                            Log.d(Const.LOG_TAG, "Kill UDP Channel ID = " + data.getSrcPort());
                        key.channel().close();
                        key.cancel();
                        mChannels.remove(data.getSrcPort());
                    }
                }
            }
        }
    }

}