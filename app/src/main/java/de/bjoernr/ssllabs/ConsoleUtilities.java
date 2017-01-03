package de.bjoernr.ssllabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConsoleUtilities 
{
	private static String newLine = "\n";
	
	/*
	 * Special thanks to Vikas Gupta (http://stackoverflow.com/users/2915208/vikas-gupta)
	 * http://stackoverflow.com/questions/21720759/convert-a-json-string-to-a-hashmap
	 * 
	 * START code of Vikas Gupta from stackoverflow
	 */
	public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
	    Map<String, Object> retMap = new HashMap<String, Object>();

	    if(json != JSONObject.NULL) {
	        retMap = toMap(json);
	    }
	    return retMap;
	}

	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
	    Map<String, Object> map = new HashMap<String, Object>();

	    Iterator<String> keysItr = object.keys();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        Object value = object.get(key);

	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        map.put(key, value);
	    }
	    return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
	    List<Object> list = new ArrayList<Object>();
	    for(int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        list.add(value);
	    }
	    return list;
	}
	// END code of Vikas Gupta from stackoverflow
	
	public static String mapToConsoleOutput(Map<String, Object> map)
	{
		String consoleOutput = "";
		
		for(Map.Entry<String, Object> entry : map.entrySet())
		{
			consoleOutput += entry.getKey() + " = " + entry.getValue().toString();
			consoleOutput += newLine;
		}
		
		return (consoleOutput);
	}
	
	public static String arrayValueMatchRegex(String[] array, String regex)
	{
		Pattern p = Pattern.compile(regex);
		
		for(int i=0; i<array.length; i++)
		{
			Matcher m = p.matcher(array[i]);
			
			while(m.find())
			{
				try
				{
					return(m.group(1));
				}
				catch (Exception ignored)
				{
					//possible IndexOutOfBoundsException
				}
 			}
		}
		
		return null;
	}
}
