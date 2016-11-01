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

package de.felixschiller.tlsmetric.DumpHandler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.felixschiller.tlsmetric.Assistant.Const;
import de.felixschiller.tlsmetric.Assistant.ContextSingleton;
import de.felixschiller.tlsmetric.Assistant.ExecuteCommand;
import de.felixschiller.tlsmetric.PacketProcessing.AnalyserService;
import de.felixschiller.tlsmetric.R;

/**
 * Handel the execution (armabi port) of tcpdump binary, pcap files and and the analyzer service.
 */
public class DumpHandler {

    //start the tcpdump process
    public static void start(){
        File bin = new File(ContextSingleton.getContext().getFilesDir(), Const.FILE_TCPDUMP);
        File file = new File(ContextSingleton.getContext().getFilesDir(), Const.FILE_DUMP);
        if (!bin.exists()){
            deployTcpDump(ContextSingleton.getContext());
        } else {
            if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, "tcpdump present.");
        }
        if (file.exists()){
            deleteDumpFile();
        }
        //kill existing processes
        stop();

        //Start tcp dump with su rights
        String command = DumpHandler.generateCommand();
        if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Try to start tcpdump");
        ExecuteCommand.sudo(command);
    }

    //stop the tcpdump process
    public static void stop(){
        ExecuteCommand.sudo("killall tcpdump");
    }

    //Extract the tcpdump binary to /system/bin folder
    public static void deployTcpDump(Context context){
        File bin = new File(ContextSingleton.getContext().getFilesDir(), Const.FILE_TCPDUMP);
        try {
            if(Const.IS_DEBUG) Log.d(Const.LOG_TAG, "Extract tcpdump.");
            InputStream in = context.getResources().openRawResource(R.raw.tcpdump);
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            OutputStream out = new FileOutputStream(bin);
            out.write(buffer);
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, "Deserialization of binary files failed", e);
        }
        ExecuteCommand.user("chmod 6755 " + bin.getAbsolutePath());
    }

        /*Run dump on active interface.
    -----------------------------------------------------------------
     *SYNOPSIS

       tcpdump [ -AdDeflLnNOpqRStuUvxX ] [ -c count ]
               [ -C file_size ] [ -F file ]
               [ -i interface ] [ -m module ] [ -M secret ] [ -r file ]
               [ -s snaplen ] [ -T type ] [ -w file ]
               [ -W filecount ] [ -E spi@ipaddr algo:secret,...  ]
               [ -y datalinktype ] [ -Z user ]
      [ expression ]
     */

    //generate command parameters
    public static String generateCommand() {
        File bin = new File(ContextSingleton.getContext().getFilesDir(), Const.FILE_TCPDUMP);
        File file = new File(ContextSingleton.getContext().getFilesDir(), Const.FILE_DUMP);
        return bin.getAbsolutePath() + " " + Const.PARAMS + " " + file.getAbsolutePath() + " &";
    }

    //remove the pcap file
    public static void deleteDumpFile(){
        File file = new File(ContextSingleton.getContext().getFilesDir(), Const.FILE_DUMP);
        deleteFile(file);
    }

    //remove specific file
    private static void deleteFile(File file){
        try{
            if(file.delete()){
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, file.getName() + " is deleted!");
            }else{
                Log.e(Const.LOG_TAG, "Delete operation failed!");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //start the service with tcpdump
    public static void startAnalyzerService(){
        File file = new File(ContextSingleton.getContext().getFilesDir(), Const.FILE_DUMP);
        for(int i = 0; i < 10; i++){
            if(file.exists()) {
                if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Dump file present, edit permissions.");
                ExecuteCommand.sudo("chmod 6755 " + file.getAbsolutePath());
                Intent intent = new Intent(ContextSingleton.getContext(), AnalyserService.class);
                ContextSingleton.getActivity().startService(intent);
                break;
            } else {
                Log.i(Const.LOG_TAG, file.getAbsolutePath() + "does not exist. Wait for dump process " + (10 - i) + " times...");
                if(i == 9) {
                    Log.e(Const.LOG_TAG, file.getAbsolutePath() + "does not exist. Service not started");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Self-destruct sequence initiated. Please evacuate service immediately.
    public static void stopAnalyzerService(){
        if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Set AnalyzerService interrupt.");
        AnalyserService.mInterrupt = true;
    }



    //Checks for su rights
    //TODO: depreached, replace with own method
    public void checkSu() {
        if (RootTools.isRootAvailable()) {
            Toast toast = Toast.makeText(ContextSingleton.getContext(), "Superuser is installed.", Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(ContextSingleton.getContext(), "Superuser is NOT installed. \n" +
                    "opening download screen", Toast.LENGTH_LONG);
            toast.show();
        }
    }




}
