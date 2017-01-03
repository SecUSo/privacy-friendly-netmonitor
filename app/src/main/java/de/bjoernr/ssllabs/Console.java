package de.bjoernr.ssllabs;

import de.bjoernr.ssllabs.Api;
import de.bjoernr.ssllabs.ConsoleUtilities;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Command line interface class
 * 
 * @author Björn Roland <https://github.com/bjoernr-de>
 */
public class Console {

	public static void main(String[] args) 
	{
		printHeader();
		
		if(args.length == 1 && (args[0].equals("--info") || args[0].equals("-i")))
		{
			handleInfo();
		}
		else if((args.length > 0 && args.length <= 6) && (args[0].equals("--host-information") || args[0].equals("-hi")))
		{
			handleHostInformation(args);
		}
		else
		{
			printUsage();
		}
	}
	
	public static void handleInfo()
	{
		Api ssllabsApi = new Api();
		
		JSONObject apiInfo = ssllabsApi.fetchApiInfo();
		Map<String, Object> map = null;
		try {
			map = ConsoleUtilities.jsonToMap(apiInfo);
		} catch (JSONException ignore){

		}
		System.out.println("API information");
		System.out.println("");
		System.out.println(ConsoleUtilities.mapToConsoleOutput(map));
	}
	
	public static void handleHostInformation(String[] args)
	{	
		//API parameters
		String host = ConsoleUtilities.arrayValueMatchRegex(args, "-h=(.+)");
		boolean publish = false;
		boolean startNew = false;
		boolean fromCache = false;
		String maxAge = null;
		String all = null;
		boolean ignoreMismatch = false;
		
		if(host == null)
		{
			//host not found in arguments
			printUsage();
			return;
		}
		
		String[] possibleArguments = {"-p", "-c", "-m", "-a", "-i"};
		
		for(String arg : possibleArguments)
		{
			if(ConsoleUtilities.arrayValueMatchRegex(args, arg) == null)
			{
				//if argument is not in args array, continue with next possible argument
				continue;
			}
			
			switch(arg)
			{
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
	
	public static void printHeader()
	{
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
	
	public static void printUsage()
	{
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