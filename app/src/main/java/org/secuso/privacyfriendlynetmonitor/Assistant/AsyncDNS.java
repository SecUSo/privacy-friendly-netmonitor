package org.secuso.privacyfriendlynetmonitor.Assistant;

import android.os.AsyncTask;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;

/**
 * Helper class, executes asynchronous DNS requests.
 */
public class AsyncDNS extends AsyncTask<String, Void, String> {

        //execute reverse hostname resolving in Collector class
        @Override
        protected String doInBackground(String... params) {
                Collector.resolveHosts();
        return "Executed!";
        }
 }