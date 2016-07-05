package com.caitu99.lsp.utils.cookie;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;

public class CookieHelper {

	public static List<HttpCookieEx> getCookies(List<String> cookieStrs) {
        List<HttpCookieEx> newCookies = new ArrayList<>();
        for (String str : cookieStrs) {
            List<HttpCookieEx> cookies = HttpCookieEx.parse(str);
            for (HttpCookieEx cookie : cookies) {
                if (!cookie.hasExpired() && !newCookies.contains(cookie)) {
                    newCookies.add(cookie);
                }
            }
        }
        return newCookies;
    }


	public static void getCookies(List<HttpCookieEx> cookieList, HttpResponse response) {
		// for Set-Cookie
		Header[] headers = response.getHeaders("Set-Cookie");
		for (Header header : headers) {
			List<HttpCookieEx> cookies = HttpCookieEx.parse(header.toString());
			for (HttpCookieEx cookie : cookies) {
				if (!cookie.hasExpired() && !cookieList.contains(cookie)) {
					cookieList.add(cookie);
				}
			}
		}

		// for Set-Cookie2
		headers = response.getHeaders("Set-Cookie2");
		for (Header header : headers) {
			List<HttpCookieEx> cookies = HttpCookieEx.parse(header.toString());
			for (HttpCookieEx cookie : cookies) {
				if (!cookie.hasExpired()) {
					int t = cookieList.indexOf(cookie);
					if (t == -1) {
						cookieList.add(cookie);
					} else {
						cookieList.set(t, cookie);
					}
				}
			}
		}
	}
	
	public static void getCookiesFresh(List<HttpCookieEx> cookieList, HttpResponse response) {
		// for Set-Cookie
		Header[] headers = response.getHeaders("Set-Cookie");
		for (Header header : headers) {
			List<HttpCookieEx> cookies = HttpCookieEx.parse(header.toString());
			for (HttpCookieEx cookie : cookies) {
				 if (!cookie.hasExpired()) {
					 int t = cookieList.indexOf(cookie);
					 if (t == -1) {
						 cookieList.add(cookie);
					 } else {
						 cookieList.set(t, cookie);
					 }
				 }
			}
		}

		// for Set-Cookie2
		headers = response.getHeaders("Set-Cookie2");
		for (Header header : headers) {
			List<HttpCookieEx> cookies = HttpCookieEx.parse(header.toString());
			for (HttpCookieEx cookie : cookies) {
				if (!cookie.hasExpired()) {
					int t = cookieList.indexOf(cookie);
					if (t == -1) {
						cookieList.add(cookie);
					} else {
						cookieList.set(t, cookie);
					}
				}
			}
		}
	}

	public static void setCookies(String uriStr, HttpMessage httpGet, List<HttpCookieEx> cookieList)
			throws URISyntaxException {

		List<HttpCookieEx> neededCookies = new ArrayList<>();
		URI uri = new URI(uriStr);
		String path = uri.getPath();
		boolean secureLink = "https".equalsIgnoreCase(uri.getScheme());

		for (HttpCookieEx cookie : cookieList) {
			if (!cookie.hasExpired() && pathMatches(path, cookie.getPath()) && (secureLink || !cookie.getSecure())) {
				if (cookie.isHttpOnly()) {
					String s = uri.getScheme();
					if (!"http".equalsIgnoreCase(s) && !"https".equalsIgnoreCase(s)) {
						continue;
					}
				}
				String ports = cookie.getPortlist();
				if (ports != null && !ports.isEmpty()) {
					int port = uri.getPort();
					if (port == -1) {
						port = "https".equals(uri.getScheme()) ? 443 : 80;
					}
					if (isInPortList(ports, port)) {
						neededCookies.add(cookie);
					}
				} else {
					neededCookies.add(cookie);
				}
			}
		}

		List<String> cookieHeader = sortByPath(neededCookies);
		String value = StringUtils.join(cookieHeader, "; ");
		httpGet.setHeader("Cookie", value);
	}

	public static void setCookies2(String uriStr, HttpMessage httpGet, List<HttpCookieEx> cookieList)
			throws URISyntaxException {

		List<HttpCookieEx> neededCookies = new ArrayList<>();
		URI uri = new URI(uriStr);
		String path = uri.getPath();
		boolean secureLink = "https".equalsIgnoreCase(uri.getScheme());

		for (HttpCookieEx cookie : cookieList) {
			if (!cookie.hasExpired() && (secureLink || !cookie.getSecure())) {
				if (cookie.isHttpOnly()) {
					String s = uri.getScheme();
					if (!"http".equalsIgnoreCase(s) && !"https".equalsIgnoreCase(s)) {
						continue;
					}
				}
				String ports = cookie.getPortlist();
				if (ports != null && !ports.isEmpty()) {
					int port = uri.getPort();
					if (port == -1) {
						port = "https".equals(uri.getScheme()) ? 443 : 80;
					}
					if (isInPortList(ports, port)) {
						neededCookies.add(cookie);
					}
				} else {
					neededCookies.add(cookie);
				}
			}
		}

		List<String> cookieHeader = sortByPath(neededCookies);
		String value = StringUtils.join(cookieHeader, "; ");
		httpGet.setHeader("Cookie", value);
	}

	public static String getSpecCookieValue(String key, List<HttpCookieEx> cookieList) {
		if (cookieList == null)
			return null;
		for (HttpCookieEx cookie : cookieList) {
			if (cookie.getName().equals(key)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private static boolean pathMatches(String path, String pathToMatchWith) {
		if (path == pathToMatchWith)
			return true;
		if (path == null || pathToMatchWith == null)
			return false;
		if (path.startsWith(pathToMatchWith))
			return true;

		return false;
	}

	private static boolean isInPortList(String lst, int port) {
		int i = lst.indexOf(",");
		int val = -1;
		while (i > 0) {
			try {
				val = Integer.parseInt(lst.substring(0, i));
				if (val == port) {
					return true;
				}
			} catch (NumberFormatException numberFormatException) {
			}
			lst = lst.substring(i + 1);
			i = lst.indexOf(",");
		}
		if (!lst.isEmpty()) {
			try {
				val = Integer.parseInt(lst);
				if (val == port) {
					return true;
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return false;
	}

	private static List<String> sortByPath(List<HttpCookieEx> cookies) {
		Collections.sort(cookies, new CookiePathComparator());

		List<String> cookieHeader = new java.util.ArrayList<String>();
		for (HttpCookieEx cookie : cookies) {
			// Netscape cookie spec and RFC 2965 have different format of Cookie
			// header; RFC 2965 requires a leading $Version="1" string while
			// Netscape
			// does not.
			// The workaround here is to add a $Version="1" string in advance
			if (cookies.indexOf(cookie) == 0 && cookie.getVersion() > 0) {
				cookieHeader.add("$Version=\"1\"");
			}

			cookieHeader.add(cookie.toString());
		}
		return cookieHeader;
	}

	private static class CookiePathComparator implements Comparator<HttpCookieEx> {
		public int compare(HttpCookieEx c1, HttpCookieEx c2) {
			if (c1 == c2)
				return 0;
			if (c1 == null)
				return -1;
			if (c2 == null)
				return 1;

			// path rule only applies to the cookies with same name
			if (!c1.getName().equals(c2.getName()))
				return 0;

			// those with more specific Path attributes precede those with less
			// specific
			if (c1.getPath().startsWith(c2.getPath()))
				return -1;
			else if (c2.getPath().startsWith(c1.getPath()))
				return 1;
			else
				return 0;
		}
	}

	/**
	 * 获取cookie串
	 * 
	 * @Description: (方法职责详细描述,可空)
	 * @Title: getCookieStr
	 * @param uriStr
	 * @param response
	 * @return
	 * @throws URISyntaxException
	 * @date 2015年11月19日 下午6:11:30
	 * @author ws
	 */
	public static String getCookieStr(String uriStr, HttpResponse response) throws URISyntaxException {

		List<HttpCookieEx> cookieList = new ArrayList<HttpCookieEx>();
		getCookies(cookieList, response);

		return getCookieStr(uriStr, cookieList);
	}

	/**
	 * 获取cookie字符串
	 * 
	 * @Description: (方法职责详细描述,可空)
	 * @Title: getCookieStr
	 * @param uriStr
	 * @param cookieList
	 * @return
	 * @throws URISyntaxException
	 * @date 2015年11月19日 下午6:14:47
	 * @author ws
	 */
	public static String getCookieStr(String uriStr, List<HttpCookieEx> cookieList) throws URISyntaxException {
		List<HttpCookieEx> neededCookies = new ArrayList<>();
		URI uri = new URI(uriStr);
		String path = uri.getPath();
		boolean secureLink = "https".equalsIgnoreCase(uri.getScheme());

		for (HttpCookieEx cookie : cookieList) {
			if (pathMatches(path, cookie.getPath()) && (secureLink || !cookie.getSecure())) {
				if (cookie.isHttpOnly()) {
					String s = uri.getScheme();
					if (!"http".equalsIgnoreCase(s) && !"https".equalsIgnoreCase(s)) {
						continue;
					}
				}
				String ports = cookie.getPortlist();
				if (ports != null && !ports.isEmpty()) {
					int port = uri.getPort();
					if (port == -1) {
						port = "https".equals(uri.getScheme()) ? 443 : 80;
					}
					if (isInPortList(ports, port)) {
						neededCookies.add(cookie);
					}
				} else {
					neededCookies.add(cookie);
				}
			}
		}

		List<String> cookieHeader = sortByPath(neededCookies);
		String value = StringUtils.join(cookieHeader, "; ");
		return value;
	}

}
