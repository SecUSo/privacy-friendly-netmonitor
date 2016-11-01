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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.voytechs.jnetstream.codec.Decoder;
import com.voytechs.jnetstream.codec.Packet;
import com.voytechs.jnetstream.io.EOPacketStream;
import com.voytechs.jnetstream.io.RawformatInputStream;
import com.voytechs.jnetstream.io.StreamFormatException;
import com.voytechs.jnetstream.npl.SyntaxError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.felixschiller.tlsmetric.Activities.EvidenceActivity;
import de.felixschiller.tlsmetric.Activities.MainActivity;
import de.felixschiller.tlsmetric.Assistant.Const;
import de.felixschiller.tlsmetric.Assistant.ContextSingleton;
import de.felixschiller.tlsmetric.R;
import de.felixschiller.tlsmetric.DumpHandler.DumpHandler;


/**
 * Packet Analyzer Service. Working with VPN- or Dump-core, set by boolean.
 *
 * Each packet captured by a dump-core is checked for protocol filters.
 */
public class AnalyserService extends Service {

    public static boolean mInterrupt;
    private Thread mThread;
    private RawformatInputStream mRawIn;
    private Decoder mDecoder;
    private File mDumpFile;
    private long mBufferPosition;
    private boolean mIsFileEmpty;
    private boolean isVpn;
    private Evidence mEvidence = new Evidence();

    private int mNotificationCount;
    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.icon)
                    .setContentTitle("TLSMetric")
                    .setContentText("Packet analyzer service is running.");

    //private Bitmap mQuest;
    private Bitmap mOk;
    private Bitmap mWarnOrange;
    private Bitmap mWarnRed;


    @Override
    public void onCreate() {
        mInterrupt = false;
        isVpn = false;
        mBufferPosition = 0;
        mNotificationCount = 0;
        loadNotificationBitmaps();

        showAppNotification();

        if(!isVpn){
            mDumpFile = new File(ContextSingleton.getContext().getFilesDir() + File.separator + Const.FILE_DUMP);
            initDecoderWithDumpfile();
        } else {
            //VPN branch : Init mDecoder set to CloneBuffer
            Log.i(Const.LOG_TAG,"VPN core not yet implemented");
        }
    }

    //Icons for Notification manager. Must be converted to bitmaps.
    private void loadNotificationBitmaps() {
        //mQuest = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_quest, mBitmapOptions);
        mOk = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_ok);
        mWarnOrange = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_warn_orange);
        mWarnRed = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_warn_red);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        showAppNotification();
        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
        }

        //Analyzer working thread
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Packet packet;
                    while (!mInterrupt) {
                        packet = dumpNext();
                        if(packet != null){
                            if(mEvidence.processPacket(packet)){
                                checkForNotifications();
                            }
                            Thread.sleep(50);
                        } else {
                            Thread.sleep(1000);
                            if(Const.IS_DEBUG)Log.d(Const.LOG_TAG, "Reinitialize dumpfile");
                            initDecoderWithDumpfile();
                        }
                        checkForNotifications();
                    }
                } catch (SyntaxError | IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                if(mInterrupt)mThread.interrupt();
                stopSelf();
            }
        }, "AnalyzerThreadRunnable");

        //start the service
        mThread.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        showNoNotification();
        DumpHandler.deleteDumpFile();
        Toast.makeText(this, "TLSMetric service stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AnalyzerBinder();
    }

    /*
    * Returns the dumped Packet or null. Null means no packet is availiable and thread will sleep.
    * If the dumpfile is empty a new initialization attempt will be made.
    */
    private Packet dumpNext() throws IOException, SyntaxError {

        if (!isVpn && !mIsFileEmpty) {
            try {
                return mDecoder.nextPacket();
            } catch (StreamFormatException e) {
                if (Const.IS_DEBUG)
                    Log.d(Const.LOG_TAG, "No complete Packet in file, taking a little break...");
                e.printStackTrace();
                return null;
            }
        } else if (mIsFileEmpty) {
            //if (Const.IS_DEBUG) Log.d(Const.LOG_TAG, "File is Empty. Reinitialize.");
            initDecoderWithDumpfile();
            return null;
        } else if (isVpn) {
            // VPN branch - read from CloneBuffer
            Log.i(Const.LOG_TAG,"VPN core not yet implemented");
            return null;
        }
        return null;
    }

    //File empty?
    private void checkEmptyFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            int b = fis.read();
            mIsFileEmpty = b == -1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Fill decoder of the framework
    private void initDecoderWithDumpfile() {
        try {
            if (mDumpFile.exists()) {
                checkEmptyFile(mDumpFile);
                if(!mIsFileEmpty) {
                    mRawIn = new RawformatInputStream(mDumpFile.getAbsolutePath());
                    mRawIn.skip(mBufferPosition);
                    mDecoder = new Decoder(mRawIn);
                }
            } else{
                Log.e(Const.LOG_TAG, "Could not find raw Dump file " + mDumpFile.getAbsolutePath());
            }
        } catch (IOException | SyntaxError | EOPacketStream | StreamFormatException e) {
            e.printStackTrace();
        }
    }
    private void checkForNotifications(){
        if(Evidence.newWarnings != mNotificationCount) {
            mNotificationCount = Evidence.newWarnings;
            if (mNotificationCount > 0) {
                showWarningNotification();
            } else {
                showAppNotification();
            }
        }
    }

    //BG notification. Standard Android version.
    private void showAppNotification(){
        mBuilder.setSmallIcon(R.mipmap.icon);
        mBuilder.setLargeIcon(mOk);
            Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(Const.LOG_TAG, 1, mBuilder.build());
    }

    //Computes the need and severity of a notification.
    private void showWarningNotification(){
        //Set corresponding icon
        if(Evidence.getMaxSeverity() > 2){
            mBuilder.setSmallIcon(R.mipmap.icon_warn_red);
            mBuilder.setLargeIcon(mWarnRed);
        } else {
            mBuilder.setSmallIcon(R.mipmap.icon_warn_orange);
            mBuilder.setLargeIcon(mWarnOrange);
        }
        mBuilder.setContentText(mNotificationCount + " new warnings encountered.");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, EvidenceActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(EvidenceActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(Const.LOG_TAG, 1, mBuilder.build());
    }

    //Revoke notifications
    private void showNoNotification(){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    //For future IPC implementations
    public class AnalyzerBinder extends Binder {
        AnalyserService getService() {
            return AnalyserService.this;
        }
    }


}
