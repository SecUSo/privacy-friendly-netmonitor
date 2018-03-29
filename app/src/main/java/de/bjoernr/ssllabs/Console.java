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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Command line interface class
 *
 * @author Björn Roland <https://github.com/bjoernr-de>
 */
public class Console {

    public static void main(String[] args) {
        printHeader();

        if (args.length == 1 && (args[0].equals("--info") || args[0].equals("-i"))) {
            handleInfo();
        } else if ((args.length > 0 && args.length <= 6) && (args[0].equals("--host-information") || args[0].equals("-hi"))) {
            handleHostInformation(args);
        } else {
            printUsage();
        }
    }

    public static void handleInfo() {
        Api ssllabsApi = new Api();

        JSONObject apiInfo = ssllabsApi.fetchApiInfo();
        Map<String, Object> map = null;
        try {
            map = ConsoleUtilities.jsonToMap(apiInfo);
        } catch (JSONException ignore) {

        }
        System.out.println("API information");
        System.out.println("");
        System.out.println(ConsoleUtilities.mapToConsoleOutput(map));
    }

    public static void handleHostInformation(String[] args) {
        //API parameters
        String host = ConsoleUtilities.arrayValueMatchRegex(args, "-h=(.+)");
        boolean publish = false;
        boolean startNew = false;
        boolean fromCache = false;
        String maxAge = null;
        String all = null;
        boolean ignoreMismatch = false;

        if (host == null) {
            //host not found in arguments
            printUsage();
            return;
        }

        String[] possibleArguments = {"-p", "-c", "-m", "-a", "-i"};

        for (String arg : possibleArguments) {
            if (ConsoleUtilities.arrayValueMatchRegex(args, arg) == null) {
                //if argument is not in args array, continue with next possible argument
                continue;
            }

            switch (arg) {
                case "-p":
                    publish = true;
                    break;
                case "-c":
                    fromCache = true;
                    break;
                case "-m":
                    maxAge = ConsoleUtilities.arrayValueMatchRegex(args, "-m=(.+)");
                    break;
                case "-a":
                    all = ConsoleUtilities.arrayValueMatchRegex(args, "-a=(.+)");
                    break;
                case "-i":
                    ignoreMismatch = true;
                    break;
            }
        }

        Api ssllabsApi = new Api();

        JSONObject hostInformation = ssllabsApi.fetchHostInformation(host, publish, startNew, fromCache, maxAge, all, ignoreMismatch);

        Map<String, Object> map = null;
        try {
            map = ConsoleUtilities.jsonToMap(hostInformation);
        } catch (JSONException ignore) {
        }

        System.out.println("Host information");
        System.out.println("");
        System.out.println(ConsoleUtilities.mapToConsoleOutput(map));
    }

    public static void printHeader() {
        System.out.println("");
        System.out.println("   ___                    _____ _____ _      _           _            ___  ______ _____ ");
        System.out.println("  |_  |                  /  ___/  ___| |    | |         | |          / _ \\ | ___ \\_   _|");
        System.out.println("    | | __ ___   ____ _  \\ `--.\\ `--.| |    | |     __ _| |__  ___  / /_\\ \\| |_/ / | |  ");
        System.out.println("    | |/ _` \\ \\ / / _` |  `--. \\`--. \\ |    | |    / _` | '_ \\/ __| |  _  ||  __/  | |  ");
        System.out.println("/\\__/ / (_| |\\ V / (_| | /\\__/ /\\__/ / |____| |___| (_| | |_) \\__ \\ | | | || |    _| |_ ");
        System.out.println("\\____/ \\__,_| \\_/ \\__,_| \\____/\\____/\\_____/\\_____/\\__,_|_.__/|___/ \\_| |_/\\_|    \\___/ ");
        System.out.println("by Bjoern Roland <https://github.com/bjoernr-de>");
        System.out.println("and contributors (https://github.com/bjoernr-de/java-ssllabs-api/graphs/contributors)");
        System.out.println("-------------------------------------------------");
        System.out.println("");
    }

    public static void printUsage() {
        String jarName = "java-ssllabs-api-" + Api.getVersion() + ".jar";
        String jarExecution = "java -jar " + jarName;

        System.out.println("Help");
        System.out.println(jarExecution);
        System.out.println("");

        System.out.println("-i, --info");
        System.out.println("	Fetch API information");
        System.out.println("");
        System.out.println("-hi, --host-information");
        System.out.println("	Mandatory parameter:");
        System.out.println("	-h, --host (String)");
        System.out.println("");
        System.out.println("	Additional parameter:");
        System.out.println("	-p, --publish (boolean) - default value is false");
        System.out.println("	-c, --fromCache (boolean) - default value is false");
        System.out.println("	-m, --maxAge (String)");
        System.out.println("	-a, --all (String)");
        System.out.println("	-i, --ignoreMismatch (boolean) - default value is false");
        System.out.println("");
        System.out.println("	Example:");
        System.out.println("	" + jarExecution + " -hi -h=https://ssllabs.com -p -c -m=\"1\"");
    }
}