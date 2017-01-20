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
package org.secuso.privacyfriendlynetmonitor.Assistant;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bjoernr.ssllabs.Api;
import de.bjoernr.ssllabs.ConsoleUtilities;

/**
 * Class for performing asynchronous requests of JSON Objects via SSL-Labs API
 *
 * Qualis SSL Labs API: https://www.ssllabs.com/projects/ssllabs-apis
 * Java-SSLLabs-API Björn Roland and Qualis SSL Labs: https://github.com/bjoernr-de
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

    //Fetch cached information from SSL Labs using list of hostnames
    private void fetchHostInfo(List<String> urls) {
        int count = getMaxAssessments();
        JSONObject hostInfo;
        String host;
        ArrayList<String> pendingList = new ArrayList<>();
        while(count > 0 && urls.size() > 0){
            host = urls.get(0);
            hostInfo = mSSLLabsApi.fetchHostInformationCached(host, null, false, false);

            // add to map if not empty
            Map<String, Object> map = null;
            try { map = ConsoleUtilities.jsonToMap(hostInfo); } catch (JSONException ignore){}
            if(map != null && map.size() > 0){
                Collector.mCertValMap.put(host, map);
            }
            //continue to resolve if request not ready
            if(map != null && map.size() > 0 && !Collector.analyseReady(map)){
                pendingList.add(host);
            }
            urls.remove(0);
            count--;

            if(Const.IS_DEBUG){Log.d(Const.LOG_TAG, ConsoleUtilities.mapToConsoleOutput(map));}
        }
        // manage pending lists
        Collector.sCertValList.addAll(pendingList);
        Collector.updateCertHostHandler();
    }

    // Get number off allowed request at the time
    private int getMaxAssessments() {
        final String max = "maxAssessments";
        JSONObject hostInfo = mSSLLabsApi.fetchApiInfo();

        Map<String, Object> map = null;
        try { map = ConsoleUtilities.jsonToMap(hostInfo); } catch (JSONException ignore) {}
        if(Const.IS_DEBUG){Log.d(Const.LOG_TAG, ConsoleUtilities.mapToConsoleOutput(map));}
        if (map.containsKey(max)) {
            return (Integer) map.get(max);
        } else {
            return 0;
        }

    }
}
