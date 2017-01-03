package de.bjoernr.ssllabs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 * Java-SSLLabs-API
 * 
 * This Java library provides basic access to the SSL Labs API
 * and is build upon the official API documentation at
 * https://github.com/ssllabs/ssllabs-scan/blob/master/ssllabs-api-docs.md
 * 
 * @author Björn Roland <https://github.com/bjoernr-de>
 * @license GNU GENERAL PUBLIC LICENSE v3
 */
public class Api
{
	private static final String API_URL = "https://api.ssllabs.com/api/v2";
	private static final String VERSION = "0.0.1-SNAPSHOT";
	
	/**
	 * Fetch API information
	 * API Call: info
	 * 
	 * @return	JSONObject
	 */
	public JSONObject fetchApiInfo()
	{
		String jsonString;
		JSONObject json = new JSONObject();
		
		try 
		{
			jsonString = sendApiRequest("info", null);
			json = new JSONObject(jsonString);
		}
		catch (Exception ignored){}

		return (json);
	}
	
	/**
	 * Fetch host information
	 * API Call: analyze
	 * 
	 * @param	host
	 * @param	publish
	 * @param	startNew
	 * @param	fromCache
	 * @param	maxAge
	 * @param	all
	 * @param	ignoreMismatch
	 * @return	JSONObject
	 */
	public JSONObject fetchHostInformation(String host, boolean publish, boolean startNew, boolean fromCache, String maxAge, String all, boolean ignoreMismatch)
	{
		String jsonString;
		JSONObject json = new JSONObject();
		
		try
		{
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
		}
		catch (Exception ignored){}
		
		return (json);
	}
	
	/**
	 * Same as fetchHostInformation() but prefer caching
	 * fetchHostInformation() with proper parameters can also be used
	 * API Call: analyze
	 * 
	 * @param	host
	 * @param	maxAge
	 * @param	publish
	 * @param	ignoreMismatch
	 * @return	JSONObject
	 */
	public JSONObject fetchHostInformationCached(String host, String maxAge, boolean publish, boolean ignoreMismatch)
	{
		return (fetchHostInformation(host, publish, false, true, maxAge, "done", ignoreMismatch));
	}
	
	/**
	 * Fetch endpoint data
	 * API Call: getEndpointData
	 * 
	 * @param	host
	 * @param	s
	 * @param	fromCache
	 * @return	JSONObject
	 */
	public JSONObject fetchEndpointData(String host, String s, boolean fromCache)
	{
		String jsonString;
		JSONObject json = new JSONObject();
		
		try
		{
			Map<String, String> parameters = new HashMap<String, String>();
			
			parameters.put("host", host);
			parameters.put("s", s);
			parameters.put("fromCache", booleanToOnOffString(fromCache));
			
			jsonString = sendApiRequest("getEndpointData", parameters);
			json = new JSONObject(jsonString);
		}
		catch (Exception ignored){}
		
		return (json);
	}
	
	/**
	 * Fetch status codes
	 * API Call: getStatusCodes
	 * 
	 * @return	JSONObject
	 */
	public JSONObject fetchStatusCodes()
	{
		String jsonString;
		JSONObject json = new JSONObject();
		
		try 
		{
			jsonString = sendApiRequest("getStatusCodes", null);
			json = new JSONObject(jsonString);
		}
		catch (Exception ignored){}

		return (json);
	}
	
	/**
	 * Send custom API request and return API response
	 * 
	 * @param	apiCall
	 * @param	parameters
	 * @return	String
	 */
	public String sendCustomApiRequest(String apiCall, Map<String, String> parameters)
	{
		String jsonString = "";
		
		try
		{
			jsonString = sendApiRequest(apiCall, parameters);
		}
		catch(Exception ignored){}
		
		return (jsonString);
	}
	
	/**
	 * Sends an api request and return api response
	 * 
	 * @param	apiCall
	 * @param	parameters
	 * @return	String
	 * @throws	IOException
	 */
	private String sendApiRequest(String apiCall, Map<String, String> parameters) throws IOException
	{
		URL url = new URL(API_URL + "/" + apiCall);
		
		if(parameters != null)
		{
			url = new URL(url.toString() + buildGetParameterString(parameters));
		}
		
		InputStream is = url.openStream();
		int nextByteOfData = 0;
		
		StringBuffer apiResponseBuffer = new StringBuffer();
		
		while((nextByteOfData = is.read()) != -1)
		{
			apiResponseBuffer.append((char) nextByteOfData);
		}
					
		is.close();

		return (apiResponseBuffer.toString());
	}
	
	/**
	 * Helper function to build GET parameter string
	 * 
	 * @param	parameters
	 * @return	String
	 */
	private String buildGetParameterString(Map<String, String> parameters)
	{
		String getParameterString = "";
		
		for(Map.Entry<String, String> param : parameters.entrySet())
		{
			if(param.getValue() == null)
			{
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
	 * @param	boolean	b
	 * @return	String on|off
	 */
	private String booleanToOnOffString(boolean b)
	{
		return (b == true) ? "on" : "off";
	}
	
	/**
	 * Getter for API_URL
	 * 
	 * @return	String
	 */
	public static String getApiUrl()
	{
		return API_URL;
	}
	
	/**
	 * Getter for VERSION
	 * 
	 * @return	String
	 */
	public static String getVersion()
	{
		return VERSION;
	}
}