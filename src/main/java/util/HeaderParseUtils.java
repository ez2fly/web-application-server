package util;


import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import model.User;

public class HeaderParseUtils {
	
	public static String[] parseTargetURL(String header) {
		if (Strings.isNullOrEmpty(header)) { 
			return null;
		}
		String[] tokens = header.split(" ");
		return tokens;
	}
	
	public static boolean containsQueryString(String url) {
		return url.contains("?");
	}
	
	public static String[] getQueryString(String url) {
		if (Strings.isNullOrEmpty(url)) {
			return null;
		}
		
		if (!url.contains("?")) {
			return null;
			
		}
		return url.split("\\?");
	}
	
	public static User getUser(Map<String, String> queryMap) {
		if (queryMap == null) {
			return null;
		}
		
		String userid = queryMap.get("userId");
		String password = queryMap.get("password");
		String name = queryMap.get("name");
		String email = queryMap.get("email");
		return new User(userid, password, name, email);
	}
}
