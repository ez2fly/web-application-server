package webserver;

import java.io.BufferedReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import db.DataBase;
import util.HeaderParseUtils;
import util.HttpRequestUtils;
import util.IOUtils;

import model.User;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
//		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			DataOutputStream dos = new DataOutputStream(out);
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
			// Method, Param
			String url = getFirstLine(br);
			String[] target = HeaderParseUtils.parseTargetURL(url);
			if (target == null || target.length < 2) {
				return;
			}
			String method = target[0].trim();
			String param = target[1].trim();
//			log.debug("Method: " + method + ", Param: " + param);
			
			// Header
			Map<String, String> header = getHeader(br);
			
			// 요구사항5 처리완료후에 index.html 로 넘어갈때 Cookie 체크가 안됨. 그 다음에 페이지 넘어가면서 부터 됨. 
			// 원래 그런건지 확인 필요
			// for debug (login response)
//			String cookie = header.get("Cookie");
//			if (!Strings.isNullOrEmpty(cookie)) {
//				log.debug("Cookie " + header.get("Cookie"));
//			}
			
			if ("GET".equals(method)) {
				doGet(param, dos);
			}
			
			if ("POST".equals(method)) {
				String body = IOUtils.readData(br, Integer.parseInt(header.get("Content-Length")));
				doPost(param, body, dos);
			}
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response200HeaderByCSS(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/css\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response302Header(DataOutputStream dos, String url) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: " + url + " \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response302HeaderWithCookie(DataOutputStream dos, String url, String cookie) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: " + url + " \r\n");
			dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void printRequest(BufferedReader br) {
		log.debug("Print Header : ");
		try{
			String line = null;
			while (!"".equals(line = br.readLine())) {
				if (line == null) break;
				
				log.debug(line);
			}
		}catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private String getFirstLine(BufferedReader br) {
		String ret = null;
		try{
			ret = br.readLine();
		} catch(IOException e) {
			log.error(e.getMessage());
		}
		return ret;
	}
	
	private Map<String, String> getHeader(BufferedReader br) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		String line = null;
		while (!"".equals(line = br.readLine()) && line != null) {
			String[] tokens = line.split(": ");
			if (tokens.length != 2) {
				continue;
			}
			
			headers.put(tokens[0], tokens[1]);
		}
		return headers;
	}
	
	private void doGet(String param, DataOutputStream dos) throws IOException {
		if (Strings.isNullOrEmpty(param)) {
			return;
		}
		
		// url?param
		if (HeaderParseUtils.containsQueryString(param)) {
			String[] query = HeaderParseUtils.getQueryString(param);
			if (query == null || query.length != 2) { 
				return; 
			}
			
			if (query[0].equals("/user/create")) {
				User user = HeaderParseUtils.getUser(HttpRequestUtils.parseQueryString(query[1]));
				log.debug("/user/create");
				log.debug(user.toString());
				DataBase.addUser(user);
				
				byte[] respBody = IOUtils.readFile("./webapp" + "index.html");
				if (respBody != null) {
					response200Header(dos, respBody.length);
					responseBody(dos, respBody);
				}
			}
		}
		// css
		else if (param.endsWith(".css")) {
			byte[] respBody = IOUtils.readFile("./webapp" + param);
			if (respBody != null) {
				response200HeaderByCSS(dos, respBody.length);
				responseBody(dos, respBody);
			}
		}
		// url
		else {
			byte[] respBody = IOUtils.readFile("./webapp" + param);
			if (respBody != null) {
				response200Header(dos, respBody.length);
				responseBody(dos, respBody);
			}
		}
		
	}
	
	private void doPost(String query, String body, DataOutputStream dos) throws IOException {
		if (Strings.isNullOrEmpty(body)) {
			return;
		}
		
		switch (query) {
			case "/user/create":
			{
				User user = HeaderParseUtils.getUser(HttpRequestUtils.parseQueryString(body));
				log.debug("/user/create");
				log.debug(user.toString());
				DataBase.addUser(user);
				
				response302Header(dos, "/index.html");
			}
			break;
			case "/user/login":
			{
				Map<String, String> params = HttpRequestUtils.parseQueryString(body);
				if (params == null) {
					return;
				}
				User user = DataBase.findUserById(params.get("userId"));
				if (user != null &&
						user.getPassword().equals(params.get("password"))) {
					
					response302HeaderWithCookie(dos, "/index.html", "logined=true");
					log.debug("login success");
				} else {
					response302HeaderWithCookie(dos, "/user/login_failed.html", "logined=false");
					log.debug("login failed");
				}
			}
			break;
		}
	}
	
	
}
