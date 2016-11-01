package de.felixschiller.tlsmetric;

import org.junit.Test;

import java.sql.Timestamp;


import de.felixschiller.tlsmetric.VpnCaptureService.PacketGenerator;
import de.felixschiller.tlsmetric.VpnCaptureService.Flow.TcpFlow;
import de.felixschiller.tlsmetric.Assistant.ToolBox;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class GeneratorTests {

    private static final byte[] sIp4Dummy = PacketGenerator.hexStringToByteArray("45000086001100003d1100001111111122222222");
    private static final byte[] sIp6Dummy = PacketGenerator.hexStringToByteArray("");
    private static final byte[] sTcpDummy = PacketGenerator.hexStringToByteArray("111122221111111122222222501005ac00000000");
    private static final byte[] sUdpDummy = PacketGenerator.hexStringToByteArray("111122220072fc42");


    @Test
    public void testForgeHandshake() throws Exception {
        byte[] test = PacketGenerator.hexStringToByteArray("45000028001100003d067dbe0000000100000001000500060000000500000006501205acAA0F0000");
        TcpFlow flow = new TcpFlow(new byte[]{0x00, 0x00, 0x00, 0x01}, new byte[]{0x00, 0x00, 0x00, 0x01}, 6, 5, new Timestamp(System.currentTimeMillis()), 4);
        flow.ackNr = new byte[]{0x00, 0x00, 0x00, 0x05};
        flow.seqNr = new byte[]{0x00, 0x00, 0x00, 0x05};
        flow.flags = new byte[]{0x02};

        String b = ToolBox.printHexBinary(PacketGenerator.forgeHandshake(flow));
        String testStr = ToolBox.printHexBinary(test);
        assertEquals(testStr, b);
    }

    @Test
    public void seqIncrementerTest() throws Exception {
        byte[] b = new byte[]{(byte)0xE4, (byte)0xE6, (byte)0x7C, (byte)0x58};
        String test = ToolBox.printHexBinary(new byte[]{(byte)0xE4, (byte)0xE6, (byte)0x7C, (byte)0x59});
        b = PacketGenerator.tcpIncrementer(b, 1);
        String a = ToolBox.printHexBinary(b);
        assertEquals(a, test);
    }

    @Test
    public void testHandleFlow() throws Exception {
        TcpFlow flow = new TcpFlow(new byte[]{0x00, 0x00, 0x00, 0x01}, new byte[]{0x00, 0x00, 0x00, 0x01}, 6, 5, new Timestamp(System.currentTimeMillis()), 4);
        flow.ackNr = new byte[]{0x00, 0x00, 0x00, 0x05};
        flow.seqNr = new byte[]{(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
        flow.flags = new byte[]{0x02};

        boolean addQueue;

        long increment = PacketGenerator.fourBytesToLong(PacketGenerator.tcpIncrementer(flow.ackNr, 0));
        if(increment == PacketGenerator.fourBytesToLong(flow.ackNr)){
            addQueue = false;
        } else {
            addQueue = true;
        }
        assertEquals(PacketGenerator.fourBytesToLong(flow.ackNr), increment);

        increment = PacketGenerator.fourBytesToLong(PacketGenerator.tcpIncrementer(flow.ackNr, 1));

        if(increment == PacketGenerator.fourBytesToLong(flow.ackNr)){
            addQueue = false;
        } else{
            addQueue = true;
        }
        assertEquals(addQueue, true);

        increment = PacketGenerator.fourBytesToLong(PacketGenerator.tcpIncrementer(flow.seqNr, 1));

        if(increment == PacketGenerator.fourBytesToLong(flow.ackNr)){
            addQueue = false;
        } else {
            addQueue = true;
        }
        assertEquals(addQueue, true);
    }
}