package org.secuso.privacyfriendlytlsmetric.Assistant;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import de.bjoernr.ssllabs.Api;
import de.bjoernr.ssllabs.ConsoleUtilities;

/**
 * Created by fs on 03.01.2017.
 */

public class AsyncCertVal extends AsyncTask<Void, Void, Void>{

    private Api sSSLLabsApi;

    @Override
    public Void doInBackground(Void... voids) {
        checkAvailability();
        return null;
    }

    public JSONObject fetchHostInfo(String url) {

        return null;
    }

    public boolean checkAvailability() {

        if(sSSLLabsApi == null) { sSSLLabsApi = new Api(); }

        JSONObject hostInfo = sSSLLabsApi.fetchApiInfo();
        Map<String, Object> map = null;
        try {
            map = ConsoleUtilities.jsonToMap(hostInfo);
        } catch (JSONException ignore){

        }

        Log.d(Const.LOG_TAG, ConsoleUtilities.mapToConsoleOutput(map));

        return true;
    }


}
