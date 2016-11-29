package org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import org.secuso.privacyfriendlytlsmetric.Assistant.ContextStorage;
import org.secuso.privacyfriendlytlsmetric.R;

/**
 * This class handles commands and access to the services of the app
 * Currently Handles Services: Passive Service
 */
public class ServiceHandler {

    private PassiveService mPassiveService;
    public boolean mIsBoundPassive = false;


    //Passive service connection object
    private ServiceConnection mPassiveServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mPassiveService = ((PassiveService.AnalyzerBinder)service).getService();

            // Tell the user about this for our demo.
            Toast.makeText(ContextStorage.getContext(), R.string.passive_service_start,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mPassiveService = null;
            Toast.makeText(ContextStorage.getContext(), R.string.passive_service_stop,
                    Toast.LENGTH_SHORT).show();
        }
    };

    //Bind the passive service to app  context
    public void startPassiveService() {
        // Establish a connection with the service.
        Intent intent = new Intent(ContextStorage.getContext(),
                        PassiveService.class);
        ContextStorage.getContext().bindService(intent, mPassiveServiceConnection, Context.BIND_AUTO_CREATE);
        ContextStorage.getContext().startService(intent);
        mIsBoundPassive = true;
    }

    //Unbind the passive service to app  context
    public void stopPassiveService() {
        if (mIsBoundPassive) {
            // Detach our existing connection.
            ContextStorage.getContext().stopService(new Intent(ContextStorage.getContext(), PassiveService.class));
            ContextStorage.getContext().unbindService(mPassiveServiceConnection);
            mIsBoundPassive = false;
        }
    }

}
