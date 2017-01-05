package org.secuso.privacyfriendlytlsmetric.Assistant;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.secuso.privacyfriendlytlsmetric.ConnectionAnalysis.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bjoernr.ssllabs.Api;
import de.bjoernr.ssllabs.ConsoleUtilities;

/**
 * Created by fs on 03.01.2017.
 */

public class AsyncCertVal extends AsyncTask<Void, Void, Void>{

    private Api mSSLLabsApi;

    public AsyncCertVal() { mSSLLabsApi = new Api(); }

    @Override
    public Void doInBackground(Void... voids) {
        if(Collector.sCertValList.size() > 0) {
            fetchHostInfo(Collector.sCertValList);
        }
        return null;
    }

    // Fetch cached information in the fetch host list
    private void fetchHostInfo(List<String> urls) {
        int count = getMaxAssesments();
        JSONObject hostInfo;
        String host;
        String ip;
        ArrayList<String> pendingList = new ArrayList<>();
        while(count > 0 && urls.size() > 0){
            host = urls.get(0);
            hostInfo = mSSLLabsApi.fetchHostInformationCached(host, null, false, false);

            Map<String, Object> map = null;
            try { map = ConsoleUtilities.jsonToMap(hostInfo); } catch (JSONException ignore){}
            // add to map if not empty
            if(map != null && map.size() > 0){
                Collector.mCertValMap.put(host, map);
            }
            //continue to resolve if request not ready
            if(map != null && map.size() > 0 && !Collector.analyseReady(map)){
                pendingList.add(host);
            }
            urls.remove(0);
            count--;

            //TODO: Debug log-remove later
            Log.d(Const.LOG_TAG, ConsoleUtilities.mapToConsoleOutput(map));
        }
        Collector.sCertValList.addAll(pendingList);
        Collector.updateCertHostHandler();
    }



    // Get number off allowed request at the time
    private int getMaxAssesments() {
        final String max = "maxAssessments";
        JSONObject hostInfo = mSSLLabsApi.fetchApiInfo();

        Map<String, Object> map = null;
        try { map = ConsoleUtilities.jsonToMap(hostInfo); } catch (JSONException ignore) {}
        Log.d(Const.LOG_TAG, ConsoleUtilities.mapToConsoleOutput(map));

        if (map.containsKey(max)) {
            return (Integer) map.get(max);
        } else {
            return 0;
        }

    }
}
