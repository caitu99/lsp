package com.caitu99.lsp.utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
public class HttpClientUtils {

    private static HttpClient httpClient = null;

    private static HttpClientUtils instance = null;


    private HttpClientUtils() {
    }

    public static HttpClientUtils getInstances() {
        if (null == instance) {
            instance = new HttpClientUtils();
        }
        httpClient = new DefaultHttpClient();
        ClientConnectionManager mgr = httpClient.getConnectionManager();
        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
                httpClient.getParams(), mgr.getSchemeRegistry()), httpClient.getParams());
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
        return instance;
    }

    /**
     * Post发送https请求
     *
     * @param url      url
     * @param charset  字符编码
     * @param paramMap 参数集合
     * @return
     * @throws Exception
     * @Title: doSSLPost
     * @Description: (这里用一句话描述这个方法的作用)
     * @date 2014年5月8日 下午4:22:22
     * @author dzq
     */
//	public String doSSLPost(String url, String charset,String entityStr) throws Exception {
//		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER ;
//		try {
//	    	SchemeRegistry registry =  new  SchemeRegistry ();
//	    	SSLSocketFactory socketFactory =  SSLSocketFactory . getSocketFactory ();
//	    	socketFactory.setHostnameVerifier (( X509HostnameVerifier ) hostnameVerifier );
//	    	registry.register (new Scheme("https",443,socketFactory));
//	    	httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET,charset);
//	    	BasicClientConnectionManager bccm =  new  BasicClientConnectionManager (registry );
//	    	DefaultHttpClient client =  new  DefaultHttpClient (bccm, httpClient.getParams());
//	    	HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
//			HttpPost post = new HttpPost(url);
//			/*post.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36");
//			post.setHeader("Content-Type","application/x-www-form-urlencoded");*/
//
//
//			post.addHeader("Content-Type","application/x-www-form-urlencoded");
//	        post.addHeader("Cache-Control","max-age=0");
//	        post.setHeader("Host", "mobile.cmbchina.com");
//	        post.setHeader("Connection", "keep-alive");
//	        post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//	        post.setHeader("Upgrade-Insecure-Requests", "1");
//	        post.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36");
//	        post.setHeader("Referer", "https://mobile.cmbchina.com/MobileHtml/User/Navigation/NV_FuncSearch.aspx");
//	        //post.setHeader("Accept-Encoding", "gzip, deflate");
//	        //post.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
//	        post.setHeader("Origin", "https://mobile.cmbchina.com");
//	        post.setHeader("Cookie","ASP.NET_SessionId=wkrfvlbkl1p4rjasxpyf5lzs; LoginMode=3UFntqbNDa8_; $CLientIP$=219.82.142.229; Version=1.0.0; LoginType=C; DeviceType=H; _MobileAppVersion=1.0.0");
//
//	        StringEntity entity = new StringEntity(entityStr);
//			post.setEntity(entity );
//
//			HttpResponse httpResponse = client.execute(post);
//			int status = httpResponse.getStatusLine().getStatusCode();
//			System.out.println("status =" + status);
//			String returnString = EntityUtils.toString(httpResponse.getEntity());
//			return returnString;
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		} finally {
//			/*if (httpClient != null) {
//				httpClient.getConnectionManager().shutdown();
//			}*/
//		}
//	}
    public static void main(String[] args) throws Exception {

        StringBuffer XmlReq = new StringBuffer();

        String extPwd = "4307";
        String idNo = "131182198602066610";
        String pwd = "147258";
        XmlReq.append("<PwdC>").append(pwd).append("</PwdC>")
                .append("<ExtraPwdC>").append(extPwd).append("</ExtraPwdC>")
                .append("<LoginMode>0</LoginMode>")
                .append("<LoginByCook>false</LoginByCook>")
                .append("<IDTypeC>01</IDTypeC>")
                .append("<IDNoC>").append(idNo).append("</IDNoC>")
                .append("<RememberFlag>true</RememberFlag>")
                .append("<UserAgent>Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36</UserAgent>")
                .append("<screenW>684</screenW>")
                .append("<screenH>567</screenH>")
                .append("<OS>Win32</OS>");

        String t = XmlReq.toString().replace("<", "%3C")
                .replace(">", "%3E").replace("/", "%2F")
                .replace(";", "%3B").replace(" ", "+")
                .replace("(", "%28").replace(")", "%29")
                .replace(",", "%2C");
        System.out.println(t);
        /**
         %3C	:	<
         %3E	:	>
         %2F	:	/
         %3B	:	;
         +	:	(空格)
         %28	:	(
         %29	:	)
         %2C	:	,
         */


		/*Map<String,String> paramMap = new HashMap<String,String>();
		String url = "https://mobile.cmbchina.com/MobileHtml/CreditCard/CustomerService/CardManage/psm_QueryPoints.aspx";
		paramMap.put("ClientNo", "A22402AE8023AE1811A0E3929E544AC1082985316064682800387490");
		//A742CE28DA5CB3E465938CAF03467E29892304855127575700018135
		//A742CE28DA5CB3E465938CAF03467E29982171233299014700019809
		paramMap.put("Command", "");
		paramMap.put("XmlReq","<SearchKey>积分</SearchKey>");
		String result = HttpClientUtils.getInstances().doSSLPost(url, "UTF-8", paramMap);
		System.out.println(result);*/
    }

    public String doGet(String url) throws Exception {
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(get);
            int status = httpResponse.getStatusLine().getStatusCode();
            System.out.println("status =" + status);
            if (200 != status) {
                throw new RuntimeException("访问失败！");
            }
            String returnString = EntityUtils.toString(httpResponse.getEntity());
            return returnString;
        } catch (Exception e) {
            throw e;
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    /**
     * Post发送https请求
     *
     * @param url      url
     * @param charset  字符编码
     * @param paramMap 参数集合
     * @return
     * @throws Exception
     * @Title: doSSLPost
     * @Description: (这里用一句话描述这个方法的作用)
     * @date 2014年5月8日 下午4:22:22
     * @author dzq
     */
    public String doSSLPost(String url, String charset, Map<String, String> paramMap) throws Exception {
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        try {
            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("https", 443, socketFactory));
            httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, charset);
            BasicClientConnectionManager bccm = new BasicClientConnectionManager(registry);
            DefaultHttpClient client = new DefaultHttpClient(bccm, httpClient.getParams());
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            HttpPost post = new HttpPost(url);

            post.addHeader("Content-Type", "application/x-www-form-urlencoded");
            post.addHeader("Cache-Control", "max-age=0");
            post.setHeader("Host", "mobile.cmbchina.com");
            post.setHeader("Connection", "keep-alive");
            post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            post.setHeader("Upgrade-Insecure-Requests", "1");
            post.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36");
            post.setHeader("Referer", "https://mobile.cmbchina.com/MobileHtml/User/Navigation/NV_FuncSearch.aspx");
            //post.setHeader("Accept-Encoding", "gzip, deflate");
            //post.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
            post.setHeader("Origin", "https://mobile.cmbchina.com");
            post.setHeader("Cookie", "ASP.NET_SessionId=wkrfvlbkl1p4rjasxpyf5lzs; LoginMode=3UFntqbNDa8_; $CLientIP$=219.82.142.229; Version=1.0.0; LoginType=C; DeviceType=H; _MobileAppVersion=1.0.0");

            List<NameValuePair> nvps = converForMap(paramMap);
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps, charset);
            post.setEntity(entity);

            HttpResponse httpResponse = client.execute(post);
            int status = httpResponse.getStatusLine().getStatusCode();
            System.out.println("status =" + status);
            String returnString = EntityUtils.toString(httpResponse.getEntity());
            return returnString;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            /*if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}*/
        }
    }

    /**
     * Post发送https请求
     *
     * @param url        url
     * @param charset    字符编码
     * @param jsonString json参数
     * @return
     * @throws Exception
     * @Title: doSSLPost
     * @Description: (这里用一句话描述这个方法的作用)
     * @date 2014年5月8日 下午4:22:22
     * @author dzq
     */
    public String doSSLPost(String url, String charset, String jsonString) throws Exception {
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        try {
            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("https", 443, socketFactory));
            httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, charset);
            BasicClientConnectionManager bccm = new BasicClientConnectionManager(registry);
            DefaultHttpClient client = new DefaultHttpClient(bccm, httpClient.getParams());
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            HttpPost post = new HttpPost(url);
            StringEntity s = new StringEntity(jsonString.toString(), charset);
            s.setContentEncoding(charset);
//            s.setContentType("application/json");  

            post.setHeader("Host", "mobile.cmbchina.com");
            post.setHeader("Connection", "keep-alive");
            post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            post.setHeader("Origin", "https://mobile.cmbchina.com");
            post.setHeader("Upgrade-Insecure-Requests", "1");
            post.setHeader("Referer", "https://mobile.cmbchina.com/MobileHtml/User/Navigation/NV_FuncSearch.aspx");
//	        post.setHeader("Accept-Encoding", "gzip, deflate");
//	        post.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
            //post.setHeader("Accept-Encoding", "gzip, deflate");
            post.setHeader("Cookie", "ASP.NET_SessionId=wkrfvlbkl1p4rjasxpyf5lzs; LoginMode=3UFntqbNDa8_; $CLientIP$=219.82.142.229; Version=HTML; LoginType=C; DeviceType=H; _MobileAppVersion=1.0.0");
            post.setEntity(s);
            HttpResponse httpResponse = client.execute(post);
            int status = httpResponse.getStatusLine().getStatusCode();
            System.out.println("status =" + status);
            String returnString = EntityUtils.toString(httpResponse.getEntity());
            System.err.println(returnString);
            return returnString;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    /**
     * Post发送http请求
     *
     * @param url        url
     * @param charset    字符编码
     * @param jsonString json参数
     * @return
     * @throws Exception
     * @Title: doPost
     * @Description: (这里用一句话描述这个方法的作用)
     * @date 2014年5月8日 下午4:23:29
     * @author dzq
     */
    public String doPost(String url, String charset, String jsonString) throws Exception {
        try {
            HttpPost post = new HttpPost(url);
            StringEntity s = new StringEntity(jsonString.toString());
            s.setContentEncoding(charset);
            s.setContentType("application/json");
            post.setEntity(s);
            HttpResponse httpResponse = httpClient.execute(post);
            int status = httpResponse.getStatusLine().getStatusCode();
            System.out.println("status =" + status);
            String returnString = EntityUtils.toString(httpResponse.getEntity());
            return returnString;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    /**
     * Post发送http请求
     *
     * @param url      url
     * @param charset  字符编码
     * @param paramMap 参数集合
     * @return
     * @throws Exception
     * @Title: doPost
     * @Description: (这里用一句话描述这个方法的作用)
     * @date 2014年5月8日 下午4:24:03
     * @author dzq
     */
    public String doPost(String url, String charset, Map<String, String> paramMap) throws Exception {
        try {
            HttpPost post = new HttpPost(url);
            List<NameValuePair> nvps = converForMap(paramMap);
            post.setEntity(new UrlEncodedFormEntity(nvps, charset));
            HttpResponse httpResponse = httpClient.execute(post);
            int status = httpResponse.getStatusLine().getStatusCode();
            System.out.println("status =" + status);
            String returnString = EntityUtils
                    .toString(httpResponse.getEntity());
            return returnString;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    /**
     * 参数转换接口 Map转换
     *
     * @param signParams
     * @return
     * @Title: converForMap
     * @Description: (这里用一句话描述这个方法的作用)
     * @date 2014年4月8日 上午11:53:09
     * @author dzq
     */
    public List<NameValuePair> converForMap(Map<String, String> signParams) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keys = signParams.keySet();
        for (String key : keys) {
            nvps.add(new BasicNameValuePair(key, signParams.get(key)));
        }
        return nvps;
    }

}
