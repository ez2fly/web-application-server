package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import util.HttpRequestUtils;
import util.HttpRequestUtils.Pair;


public class HttpRequest {
	private HttpMethod method;
	private String path;
	private Map<String, String> params;
	private Map<String, String> header;
	
	public HttpRequest(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line = br.readLine();
		if (line == null) {
			return;
		}
		
		String[] tokens = line.split(" ");
		if (tokens.length != 3) {
			return;
		}
		method = parseMethod(tokens[0]);
		if (tokens[1].contains("?")) {
			String[] tt = tokens[1].split("?");
			path = tt[0];
			params = parseParameters(tt[1]);
		} else {
			path = tokens[1];
		}
		header = parseHeader(br);
		
		if (params == null && header.containsKey("Content-Length")) {
			
		}
	}
	
	public HttpMethod getMethod() {
		return HttpMethod.POST;
	}
	
	public String getHeader(String key) {
		return null;
	}
	
	public String getParameter(String key) {
		return null;
	}
	
	
	//
	// Internal Method
	private HttpMethod parseMethod(String str) {
		if ("GET".equals(str)) {
			return HttpMethod.GET;
		}
		if ("POST".equals(str)) {
			return HttpMethod.POST;
		}
		return null;
	}
	
	private Map<String, String> parseHeader(BufferedReader br) throws IOException {
		Map<String, String> ret = new HashMap<String, String>();
		
		String line = null;
		while ((line = br.readLine())!=null && !line.equals("")) {
			Pair p = HttpRequestUtils.parseHeader(line);
			if (p != null) {
				ret.put(p.getKey(), p.getValue());
			}
		}
		return ret;
	}
	
	private Map<String, String> parseParameters(String params) {
		return HttpRequestUtils.parseQueryString(params);
	}
}

