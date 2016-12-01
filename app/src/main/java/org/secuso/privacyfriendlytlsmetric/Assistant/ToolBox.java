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

package org.secuso.privacyfriendlytlsmetric.Assistant;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.PassiveService;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Enumeration;
import java.util.List;

/**
 * All the litte helpers, used by more than one layer
 */
public class ToolBox{

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    //Convert byte[] to HexString
    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    //Convert HexString to byte[]
    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    //Convert byte[] to wireshark import-string.
    public static String printExportHexString(byte[] data) {
        String hexString = printHexBinary(data);
        String export = "000000 ";
        for (int i = 0; i + 1 < hexString.length(); i += 2) {
            export += " " + hexString.substring(i, i + 2);
        }
        export += " ......";
        return export;
    }

    //Returns active network interfaces
    public String getIfs(Context context){
        //read from command: netcfg | grep UP

        String filePath = context.getFilesDir().getAbsolutePath() + File.separator + Const.FILE_IF_LIST;
        if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Try to get active interfaces to" + filePath);
        ExecuteCommand.user("rm " + filePath);
        ExecuteCommand.user("netcfg | grep UP -> " + filePath);
        String result = ExecuteCommand.userForResult("cat " + filePath);
        return result;
    }

    //Char to value
    private int hexToBin(char ch) {
        if ('0' <= ch && ch <= '9') return ch - '0';
        if ('A' <= ch && ch <= 'F') return ch - 'A' + 10;
        if ('a' <= ch && ch <= 'f') return ch - 'a' + 10;
        return -1;
    }

    //Lookup local IP address
    public static InetAddress getLocalAddress(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, "Error while obtaining local address");
            e.printStackTrace();
        }
        return null;
    }

    //Test if service is active.
    public static boolean isAnalyzerServiceRunning() {
        ActivityManager manager = (ActivityManager) ContextStorage.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PassiveService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    //Search for byte array in given byte array.
    public static int searchByteArray(byte[] input, byte[] searchedFor) {
        //convert byte[] to Byte[]
        Byte[] searchedForB = new Byte[searchedFor.length];
        for (int x = 0; x < searchedFor.length; x++) {
            searchedForB[x] = searchedFor[x];
        }

        int idx = -1;
        //search:
        Deque<Byte> q = new ArrayDeque<>(input.length);
        for (int i = 0; i < input.length; i++) {
            if (q.size() == searchedForB.length) {
                //here I can check
                Byte[] cur = q.toArray(new Byte[]{});
                if (Arrays.equals(cur, searchedForB)) {
                    //found!
                    idx = i - searchedForB.length;
                    break;
                } else {
                    //not found
                    q.pop();
                    q.addLast(input[i]);
                }
            } else {
                q.addLast(input[i]);
            }
        }
        if (Const.IS_DEBUG && idx != -1)
            Log.d(Const.LOG_TAG, ToolBox.printHexBinary(searchedFor) + " found at position " + idx);
        return idx;
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

    //Convert two bytes to a Java int
    public static int twoBytesToInt(byte[] b){
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.position(2);
        bb.put(b);
        bb.position(0);
        return bb.getInt();
    }

    //Reverse the order in a Byte array
    public static byte[] reverseByteArray(byte[] b){
        byte[] b0 = new byte[b.length];
        int j = 0;
        for(int i=b.length-1; i >= 0; i--){
            b0[j] = b[i];
            j++;
        }
        return b0;
    }

}
