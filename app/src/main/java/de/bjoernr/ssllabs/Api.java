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

    The design is based on the Privacy Friendly Example App template by Karola Marky, Christopher
    Beckmann and Markus Hau (https://github.com/SecUSo/privacy-friendly-app-example).

    Privacy Friendly Net Monitor is based on TLSMetric by Felix Tsala Schiller
    https://bitbucket.org/schillef/tlsmetric/overview.

 */
package de.bjoernr.ssllabs;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Java-SSLLabs-API
 * <p>
 * This Java library provides basic access to the SSL Labs API
 * and is build upon the official API documentation at
 * https://github.com/ssllabs/ssllabs-scan/blob/master/ssllabs-api-docs.md
 *
 * @author Björn Roland <https://github.com/bjoernr-de>
 * @license GNU GENERAL PUBLIC LICENSE v3
 */
public class Api {
    private static final String API_URL = "https://api.ssllabs.com/api/v2";
    private static final String VERSION = "0.0.1-SNAPSHOT";

    /**
     * Fetch API information
     * API Call: info
     *
     * @return JSONObject
     */
    public JSONObject fetchApiInfo() {
        String jsonString;
        JSONObject json = new JSONObject();

        try {
            jsonString = sendApiRequest("info", null);
            json = new JSONObject(jsonString);
        } catch (Exception ignored) {
        }

        return (json);
    }

    /**
     * Fetch host information
     * API Call: analyze
     *
     * @param    host
     * @param    publish
     * @param    startNew
     * @param    fromCache
     * @param    maxAge
     * @param    all
     * @param    ignoreMismatch
     * @return JSONObject
     */
    public JSONObject fetchHostInformation(String host, boolean publish, boolean startNew, boolean fromCache, String maxAge, String all, boolean ignoreMismatch) {
        String jsonString;
        JSONObject json = new JSONObject();

        try {
            Map<String, String> parameters = new HashMap<String, String>();

            parameters.put("host", host);
            parameters.put("publish", booleanToOnOffString(publish));
            parameters.put("startNew", booleanToOnOffString(startNew));
            parameters.put("fromCache", booleanToOnOffString(fromCache));
            parameters.put("maxAge", maxAge);
            parameters.put("all", all);
            parameters.put("ignoreMismatch", booleanToOnOffString(ignoreMismatch));

            jsonString = sendApiRequest("analyze", parameters);
            json = new JSONObject(jsonString);
        } catch (Exception ignored) {
        }

        return (json);
    }

    /**
     * Same as fetchHostInformation() but prefer caching
     * fetchHostInformation() with proper parameters can also be used
     * API Call: analyze
     *
     * @param    host
     * @param    maxAge
     * @param    publish
     * @param    ignoreMismatch
     * @return JSONObject
     */
    public JSONObject fetchHostInformationCached(String host, String maxAge, boolean publish, boolean ignoreMismatch) {
        return (fetchHostInformation(host, publish, false, true, maxAge, "done", ignoreMismatch));
    }

    /**
     * Fetch endpoint data
     * API Call: getEndpointData
     *
     * @param    host
     * @param    s
     * @param    fromCache
     * @return JSONObject
     */
    public JSONObject fetchEndpointData(String host, String s, boolean fromCache) {
        String jsonString;
        JSONObject json = new JSONObject();

        try {
            Map<String, String> parameters = new HashMap<String, String>();

            parameters.put("host", host);
            parameters.put("s", s);
            parameters.put("fromCache", booleanToOnOffString(fromCache));

            jsonString = sendApiRequest("getEndpointData", parameters);
            json = new JSONObject(jsonString);
        } catch (Exception ignored) {
        }

        return (json);
    }

    /**
     * Fetch status codes
     * API Call: getStatusCodes
     *
     * @return JSONObject
     */
    public JSONObject fetchStatusCodes() {
        String jsonString;
        JSONObject json = new JSONObject();

        try {
            jsonString = sendApiRequest("getStatusCodes", null);
            json = new JSONObject(jsonString);
        } catch (Exception ignored) {
        }

        return (json);
    }

    /**
     * Send custom API request and return API response
     *
     * @param    apiCall
     * @param    parameters
     * @return String
     */
    public String sendCustomApiRequest(String apiCall, Map<String, String> parameters) {
        String jsonString = "";

        try {
            jsonString = sendApiRequest(apiCall, parameters);
        } catch (Exception ignored) {
        }

        return (jsonString);
    }

    /**
     * Sends an api request and return api response
     *
     * @param    apiCall
     * @param    parameters
     * @return String
     * @throws IOException
     */
    private String sendApiRequest(String apiCall, Map<String, String> parameters) throws IOException {
        URL url = new URL(API_URL + "/" + apiCall);

        if (parameters != null) {
            url = new URL(url.toString() + buildGetParameterString(parameters));
        }

        InputStream is = url.openStream();
        int nextByteOfData = 0;

        StringBuffer apiResponseBuffer = new StringBuffer();

        while ((nextByteOfData = is.read()) != -1) {
            apiResponseBuffer.append((char) nextByteOfData);
        }

        is.close();

        return (apiResponseBuffer.toString());
    }

    /**
     * Helper function to build GET parameter string
     *
     * @param    parameters
     * @return String
     */
    private String buildGetParameterString(Map<String, String> parameters) {
        String getParameterString = "";

        for (Map.Entry<String, String> param : parameters.entrySet()) {
            if (param.getValue() == null) {
                continue;
            }

            getParameterString += (getParameterString.length() < 1) ? ("?") : ("&");

            getParameterString += param.getKey() + "=" + param.getValue();
        }

        return (getParameterString);
    }

    /**
     * Helper function to cast boolean to on/off string
     *
     * @param b
     * @return
     */
    private String booleanToOnOffString(boolean b) {
        return (b == true) ? "on" : "off";
    }

    /**
     * Getter for API_URL
     *
     * @return String
     */
    public static String getApiUrl() {
        return API_URL;
    }

    /**
     * Getter for VERSION
     *
     * @return String
     */
    public static String getVersion() {
        return VERSION;
    }
}