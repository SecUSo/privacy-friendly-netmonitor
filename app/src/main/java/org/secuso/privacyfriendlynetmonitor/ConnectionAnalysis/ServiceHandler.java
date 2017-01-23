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

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import org.secuso.privacyfriendlynetmonitor.Assistant.RunStore;
import org.secuso.privacyfriendlynetmonitor.R;

/**
 * This class handles commands and access to the services of the app
 * Currently Handles Services: Passive Service
 */
public class ServiceHandler {

    private PassiveService mPassiveService;

    private boolean mIsBound;

    //Get information of running services
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) RunStore.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private ServiceConnection mPassiveServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mPassiveService = ((PassiveService.AnalyzerBinder)service).getService();
            Toast.makeText(RunStore.getContext(), R.string.passive_service_start,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mPassiveService = null;
            Toast.makeText(RunStore.getContext(), R.string.passive_service_stop,
                    Toast.LENGTH_SHORT).show();
        }
    };

    //start the service manually
    public void startPassiveService() {
        // Establish a connection with the service.
        Intent intent = new Intent(RunStore.getAppContext(), PassiveService.class);
        RunStore.getContext().startService(intent);
    }

    //stop the passive service
    public void stopPassiveService() {
        if (isServiceRunning(PassiveService.class)) {
            RunStore.getContext().stopService(new Intent(RunStore.getAppContext(), PassiveService.class));
        }
    }

    //Bind the passive service to the assigned context
    public void bindPassiveService(Context context) {
        Intent intent = new Intent(RunStore.getAppContext(),PassiveService.class);
        context.bindService(intent, mPassiveServiceConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    //Unbind the passive service to app  context
    public void unbindPassiveService(Context context) {
        if (mIsBound) {
            context.unbindService(mPassiveServiceConnection);
            mIsBound = false;
        }
    }

}
