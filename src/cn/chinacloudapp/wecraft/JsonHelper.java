package cn.chinacloudapp.wecraft;

import java.util.*;
import org.json.*;

public class JsonHelper {
	
	public static Map<String, String> toMap(String json)
	{		
		Map result = new HashMap();
		//System.out.println("Hello, JsonHelper.toMap(json)!");
		try {
			JSONObject jsonObject = new JSONObject(json);
			Iterator iterator = jsonObject.keys();
			String key = null;
			String value = null;
			
			while(iterator.hasNext()) {
				key = (String) iterator.next();
				value = jsonObject.getString(key);
				result.put(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static ArrayList<String> toList(String json)
	{
		//System.out.println("Hello, JsonHelper.toList(json)!");
		ArrayList<String> list = new ArrayList<String>();
		try {
			JSONArray jsonArray = new JSONArray(json);
			if (jsonArray != null) {
				for(int i = 0; i < jsonArray.length(); i ++)
					list.add(jsonArray.getString(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
			
		return list;
		//return new ArrayList<String>(JsonHelper.toMap(json).values());
	}
}
