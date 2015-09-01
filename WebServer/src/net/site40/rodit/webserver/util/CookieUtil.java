package net.site40.rodit.webserver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.sun.net.httpserver.Headers;

public class CookieUtil {

	public static HashMap<String, String> getCookies(Headers request){
		List<String> cookieList = request.get("Cookie");
		if(cookieList == null)
			cookieList = new ArrayList<String>();
		HashMap<String, String> cookies = new HashMap<String, String>();
		for(String s : cookieList){
			String[] fullStr = s.split(";");
			for(String str : fullStr){
				str = str.trim();
				String[] parts = str.split(Pattern.quote("="));
				if(parts.length > 1)
					cookies.put(parts[0], parts[1]);
			}
		}
		return cookies;
	}
}
