package util;

import static org.junit.Assert.*;

import org.junit.Test;

public class HeaderParseUtilsTest {

	@Test
	public void testParseTargetURL() {
//		fail("Not yet implemented");
		String[] target = HeaderParseUtils.parseTargetURL("GET /index.html HTTP/1.1");
		assertEquals(target[1], "/index.html");
	}
	
	@Test
	public void testGetQueryString() {
		String[] spr = HeaderParseUtils.getQueryString("/user/create?userId=ez2fly&password=aron1234&name=%EA%B3%A0%EA%B8%B8%EB%8F%99&email=ez2fly%40naver.com");
		assertEquals(spr.length, 2);
	}

}
