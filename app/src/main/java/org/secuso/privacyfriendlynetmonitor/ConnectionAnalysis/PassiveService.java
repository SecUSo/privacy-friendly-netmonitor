/*
    Privacy Friendly Net Monitor (Net Monitor)
    - Copyright (2015 - 2017) Felix Tsala Schiller

    ###################################################################

    This file is part of Net Monitor.

    Net Monitor is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Net Monitor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Net Monitor.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von Net Monitor.

    Net Monitor ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Net Monitor wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.

    ###################################################################

    This app has been created in affiliation with SecUSo-Department of Technische Universität
    Darmstadt.

    Privacy Friendly Net Monitor is based on TLSMetric by Felix Tsala Schiller
    https://bitbucket.org/schillef/tlsmetric/overview.
 */

package org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis;

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


import org.secuso.privacyfriendlynetmonitor.Activities.MainActivity;
import org.secuso.privacyfriendlynetmonitor.Assistant.Const;
import org.secuso.privacyfriendlynetmonitor.Assistant.KnownPorts;
import org.secuso.privacyfriendlynetmonitor.Assistant.PrefManager;
import org.secuso.privacyfriendlynetmonitor.Assistant.RunStore;
import org.secuso.privacyfriendlynetmonitor.R;

import static java.lang.Thread.sleep;


/**
 * Report Analyzer Service. Identifies active connections on the device and invokes data
 * gathering and report compilation procedures.
 *
 */
public class PassiveService extends Service {

    public static boolean mInterrupt;
    private Thread mThread;
    private final IBinder mBinder = new AnalyzerBinder();
    private Bitmap mIcon;
    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(RunStore.getContext().getResources().getString(R.string.app_name))
                    .setContentText(RunStore.getContext().getResources().getString(R.string.bg_desc));



    @Override
    public void onCreate() {
        mInterrupt = false;
        loadNotificationBitmaps();
        showAppNotification();
        //init reserved-ports
        KnownPorts.initPortMap();
    }

    //Icons for notification manager. Must be converted to bitmaps.
    private void loadNotificationBitmaps() {
        mIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_drawer);
    }


    public void startThread() {
        Log.i(Const.LOG_TAG, "PassiveService - Thread started");
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
                        //detect connections
                        Detector.updateReportMap();
                        //check certificate validation state when feature is active
                        Collector.updateSettings();
                        if(Collector.isCertVal){Collector.updateCertVal();}
                        //sleep
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
    }

    //Call to stop service and notification
    private void interrupt(){
        showNoNotification();
        mInterrupt = true;
        stopSelf();
    }


    @Override
    public IBinder onBind(Intent intent) {
        startThread();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        interrupt();
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startThread();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        interrupt();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    //Class for Binder
    public class AnalyzerBinder extends Binder {
        PassiveService getService() {
            return PassiveService.this;
        }
    }

    //Check for new notification information. Currently inactive due to insignificance
    /*private void checkForNotifications(){
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
        mBuilder.setSmallIcon(R.drawable.ic_notification);
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

    //Computes the need and severity of a notification. Currently unused.
    private void showWarningNotification(){
        //Set corresponding icon
        //if(Evidence.getMaxSeverity() > 2){
        //    mBuilder.setSmallIcon(R.mipmap.icon_warn_red);
        //    mBuilder.setLargeIcon(mWarnRed);
        //} else {
        //    mBuilder.setSmallIcon(R.mipmap.icon_warn_orange);
        //    mBuilder.setLargeIcon(mWarnOrange);
        //}
        //mBuilder.setContentText(mNotificationCount + " new warnings encountered.");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

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

    //Revoke notifications
    private void showNoNotification(){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }


}
