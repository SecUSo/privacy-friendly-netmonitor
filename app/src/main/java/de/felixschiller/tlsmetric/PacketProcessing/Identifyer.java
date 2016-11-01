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

package de.felixschiller.tlsmetric.PacketProcessing;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import de.felixschiller.tlsmetric.Assistant.Const;
import de.felixschiller.tlsmetric.Assistant.ContextSingleton;
import de.felixschiller.tlsmetric.Assistant.ToolBox;
import de.felixschiller.tlsmetric.PacketProcessing.Filter.Filter;
import de.felixschiller.tlsmetric.PacketProcessing.Filter.Http;
import de.felixschiller.tlsmetric.PacketProcessing.Filter.Tls;
import de.felixschiller.tlsmetric.R;


/**
 * Hardcoded for protocol filters.
 * Protocols are identified by their unique header signature: ident byte[] and offset.
 *
 * TODO: Move identifiers to parsable xml
 */
public class Identifyer {


    final static byte[] sHTTP = new byte[]{(byte) 0x48, (byte) 0x54, (byte) 0x54, (byte) 0x50};
    final static byte[] sSSL3 = new byte[]{(byte) 0x03, (byte) 0x00};
    final static byte[] sTLS10 = new byte[]{(byte) 0x03, (byte) 0x01};
    final static byte[] sTLS11 = new byte[]{(byte) 0x03, (byte) 0x02};
    final static byte[] sTLS12 = new byte[]{(byte) 0x03, (byte) 0x03};

    //search for protocol match in first tcp payload bytes
    public static Filter indent(byte[] ident) {
        Filter filter = null;

        if (searchByteArray(ident, sHTTP) == 0) filter = new Http(Filter.Protocol.HTTP, 3, ContextSingleton.getContext().getResources().getString(R.string.ALERT_HTTP));
        else if (searchByteArray(ident, sSSL3) == 1 && fillSubProto(ident) != null)
            filter = new Tls(Filter.Protocol.SSL3, 1,
                    ContextSingleton.getContext().getResources().getString(R.string.ALERT_SSL_3),
                    fillSubProto(ident), 10);
        else if (searchByteArray(ident, sTLS10) == 1 && fillSubProto(ident) != null)
            filter = new Tls(Filter.Protocol.TLS10, 0,
                    ContextSingleton.getContext().getResources().getString(R.string.ALERT_TLS_10),
                    fillSubProto(ident), 10);
        else if (searchByteArray(ident, sTLS11) == 1 && fillSubProto(ident) != null)
            filter = new Tls(Filter.Protocol.TLS11, 0,
                    ContextSingleton.getContext().getResources().getString(R.string.ALERT_TLS_11),
                    fillSubProto(ident), 11);
        else if (searchByteArray(ident, sTLS12) == 1 && fillSubProto(ident) != null)
            filter = new Tls(Filter.Protocol.TLS12, 0,
                    ContextSingleton.getContext().getResources().getString(R.string.ALERT_TLS_12),
                    fillSubProto(ident), 12);
        return filter;
    }

    // TLS message type identifier
    private static Tls.TlsProtocol fillSubProto(byte[] ident) {

        switch (ident[0]) {
            case (byte) 0x16:
                return Tls.TlsProtocol.HANDSHAKE;
            case (byte) 0x15:
                return Tls.TlsProtocol.ALERT;
            case (byte) 0x17:
                return Tls.TlsProtocol.APP_DATA;
            case (byte) 0x14:
                return Tls.TlsProtocol.CHANGE_CYPHER;
            default:
                return null;
        }

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
}
