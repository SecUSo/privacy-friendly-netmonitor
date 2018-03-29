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
package bjoernr.ssllabs;

import org.json.JSONObject;
import org.junit.Test;

import de.bjoernr.ssllabs.Api;

public class ApiTest {
    @Test
    public void testFetchApiInformation() {
        Api api = new Api();
        JSONObject apiInformation = api.fetchApiInfo();

        ApiAssert.assertNotNull("JSONObject is null", apiInformation);
        ApiAssert.assertApiDataFetched(apiInformation);
    }

    @Test
    public void testFetchHostInformation() {
        Api api = new Api();
        JSONObject hostInformation = api.fetchHostInformation("https://www.ssllabs.com", false, false, false, null, null, false);

        ApiAssert.assertApiDataFetched(hostInformation);

    }

    @Test
    public void testFetchHostInformationCached() {
        Api api = new Api();
        JSONObject hostInformationCached = api.fetchHostInformationCached("https://www.sslabs.com", null, false, false);

        ApiAssert.assertApiDataFetched(hostInformationCached);
    }

    @Test
    public void testFetchEndpointData() {
        Api api = new Api();
        JSONObject endpointData = api.fetchEndpointData("https://www.ssllabs.com", "64.41.200.100", false);

        ApiAssert.assertApiDataFetched(endpointData);
    }

    @Test
    public void testFetchStatusCodes() {
        Api api = new Api();
        JSONObject statusCodes = api.fetchStatusCodes();

        ApiAssert.assertApiDataFetched(statusCodes);
    }

    @Test
    public void testFailedCustomApiRequest() {
        Api api = new Api();

        ApiAssert.assertApiResponseCode(api.getApiUrl() + "/bjoernr-de-java-ssllabs-api-test", 404);
    }
}