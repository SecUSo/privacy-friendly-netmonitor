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

package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


import org.secuso.privacyfriendlytlsmetric.Activities.ReportActivity;
import org.secuso.privacyfriendlytlsmetric.Activities.MainActivity;
import org.secuso.privacyfriendlytlsmetric.Assistant.AsyncCertVal;
import org.secuso.privacyfriendlytlsmetric.Assistant.Const;
import org.secuso.privacyfriendlytlsmetric.Assistant.KnownPorts;
import org.secuso.privacyfriendlytlsmetric.Assistant.RunStore;
import org.secuso.privacyfriendlytlsmetric.R;

import static java.lang.Thread.sleep;


/**
 * Report Analyzer Service. Identifies active connections on the device and invokes data
 * gathering and report compilation procedures.
 *
 */
public class PassiveService extends Service {

    public static boolean mInterrupt;
    private Thread mThread;
    private boolean isVpn;
    private final IBinder mBinder = new AnalyzerBinder();

    private int mNotificationCount;
    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.icon)
                    .setContentTitle("TLSMetric")
                    .setContentText("Packet analyzer service is running.");

    //private Bitmap mQuest;
    private Bitmap mIcon;


    //Class for Binders IPC connection
    public class AnalyzerBinder extends Binder {
        PassiveService getService() {
            return PassiveService.this;
        }
    }

    @Override
    public void onCreate() {
        mInterrupt = false;
        isVpn = false;
        mNotificationCount = 0;
        loadNotificationBitmaps();

        showAppNotification();

        if(isVpn){
            //VPN branch : Not implemented yet
            Log.i(Const.LOG_TAG,"VPN core not yet implemented");
        }
    }

    //Icons for Notification manager. Must be converted to bitmaps.
    private void loadNotificationBitmaps() {
        //mQuest = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_quest, mBitmapOptions);
        mIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        showAppNotification();
        //init reserved-ports
        KnownPorts.initPortMap();

        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
        }
        //Report analyzer working thread
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!mInterrupt) {
                        Detector.updateReportMap();
                        if(Collector.isCertVal){Collector.updateCertVal();}
                        sleep(1000);
                    }
                } catch (InterruptedException e) {
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
        mInterrupt = true;
        //Toast.makeText(this, "TLSMetric service stopped", Toast.LENGTH_SHORT).show();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


/*    private void checkForNotifications(){
        if(Evidence.newWarnings != mNotificationCount) {
            mNotificationCount = Evidence.newWarnings;
            if (mNotificationCount > 0) {
                showWarningNotification();
            } else {
                showAppNotification();
            }
        }
    }*/
    //BG notification. Standard Android version.
    private void showAppNotification(){
        mBuilder.setSmallIcon(R.mipmap.icon);
        mBuilder.setLargeIcon(mIcon);
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
        //if(Evidence.getMaxSeverity() > 2){
        //    mBuilder.setSmallIcon(R.mipmap.icon_warn_red);
        //    mBuilder.setLargeIcon(mWarnRed);
        //} else {
        //    mBuilder.setSmallIcon(R.mipmap.icon_warn_orange);
        //    mBuilder.setLargeIcon(mWarnOrange);
        //}
        mBuilder.setContentText(mNotificationCount + " new warnings encountered.");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ReportActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ReportActivity.class);

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


}
