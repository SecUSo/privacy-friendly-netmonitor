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
import java.util.LinkedList;
import java.util.Queue;

/**
 * Extended socket data class with flags and sequence numbers to keep track of tcp flows.
 *
 * Note: Some flags are not detected, since this implementation has no control of all tcp mechanisms.
 */
public class TcpFlow extends SocketData{


    public byte[] flags;
    public byte[] seqNr;
    public byte[] ackNr;

    public boolean syn;
    public boolean fin;
    public boolean rst;

    public boolean isBreakdown;

    public Queue<byte[]> seqQueue = new LinkedList<>();
    public Queue<byte[]> ackQueue = new LinkedList<>();

    public TcpFlow(byte[] srcAdd, byte[] dstAdd, int srcPort, int dstPort, Timestamp time, int ipVersion) {
        super(srcAdd, dstAdd, srcPort, dstPort,time, ipVersion);
        super.setTransport(Transport.TCP);
    }


}
