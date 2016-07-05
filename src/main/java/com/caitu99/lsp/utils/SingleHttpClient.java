package com.caitu99.lsp.utils;

import org.apache.http.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
public class SingleHttpClient {

    private static final Logger _logger = LoggerFactory.getLogger(SingleHttpClient.class);

    private static HttpClient httpClient = null;

    private static SingleHttpClient instance = null;

    private SingleHttpClient() {

    }

    public static SingleHttpClient getInstances() {
        if (null == instance) {
            instance = new SingleHttpClient();
        }
        httpClient = new DefaultHttpClient();
        ClientConnectionManager mgr = httpClient.getConnectionManager();
        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
                httpClient.getParams(), mgr.getSchemeRegistry()), httpClient.getParams());
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000000);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000000);
        return instance;
    }


    /**
     * http get
     *
     * @throws Exception
     * @date 2015年11月11日 下午7:25:16
     * @author lawrence
     */
    public String get(String url) throws Exception {
        return this.get(url, "UTF-8");
    }

    public String get(String url, String charset) throws Exception {
        Header[] headers = {};
        return this.get(url, charset, headers);
    }

    public String executeGet(String url, String charset, Header header) throws Exception {
        Header[] headers = {header};
        return this.get(url, charset, headers);
    }

    public String get(String url, String charset, Header[] headers) throws Exception {
        HttpEntity httpEntity = this.getEntity(url, charset, headers);
        String returnString = EntityUtils.toString(httpEntity);
        return returnString;
    }

    public HttpEntity getEntity(String url) throws Exception {
        return this.getEntity(url, "UTF-8");
    }

    public HttpEntity getEntity(String url, String charset) throws Exception {
        Header[] headers = {};
        return this.getEntity(url, charset, headers);
    }

    public HttpEntity getEntity(String url, String charset, Header[] headers) throws Exception {
        HttpResponse httpResponse = this.getResponse(url, charset, headers);
        return httpResponse.getEntity();
    }

    public HttpResponse getResponse(String url) throws Exception {
        return this.getResponse(url, "UTF-8");
    }

    public HttpResponse getResponse(String url, String charset) throws Exception {
        Header[] headers = {};
        return this.getResponse(url, charset, headers);
    }

    public HttpResponse getResponse(String url, String charset, Header[] headers) throws Exception {
        try {
            HttpGet get = new HttpGet(url);
            if (null != headers && 0 < headers.length) {
                get.setHeaders(headers);
            }
            HttpResponse httpResponse = httpClient.execute(get);
            int status = httpResponse.getStatusLine().getStatusCode();
            _logger.debug("http url {} ; status {}", url, status);
            if (HttpStatus.SC_OK != status) {
                throw new RuntimeException("访问失败！");
            }
            return httpResponse;
        } catch (Exception e) {
            _logger.error(e.getMessage());
            throw e;
        } finally {
//			if (httpClient != null) {
//				httpClient.getConnectionManager().shutdown();
//			}
        }
    }

    public String post(String url, Map<String, String> paramMap) throws Exception {
        return this.post(url, "UTF-8", paramMap);
    }

    public String post(String url, String charset, Map<String, String> paramMap) throws Exception {
        Header[] headers = {};
        return this.post(url, charset, paramMap, headers);
    }

    public String post(String url, String charset, Map<String, String> paramMap, Header[] headers) throws Exception {
        HttpEntity httpEntity = this.postEntity(url, charset, paramMap, headers);
        String returnString = EntityUtils.toString(httpEntity);
        return returnString;
    }

    public HttpEntity postEntity(String url, Map<String, String> paramMap) throws Exception {
        return this.postEntity(url, "UTF-8", paramMap);
    }

    public HttpEntity postEntity(String url, String charset, Map<String, String> paramMap) throws Exception {
        Header[] headers = {};
        return this.postEntity(url, charset, paramMap, headers);
    }

    public HttpEntity postEntity(String url, String charset, Map<String, String> paramMap, Header[] headers) throws Exception {
        HttpResponse httpResponse = this.postResponse(url, charset, paramMap, headers);
        return httpResponse.getEntity();
    }


    public HttpResponse postResponse(String url, Map<String, String> paramMap) throws Exception {
        return this.postResponse(url, "UTF-8", paramMap);
    }

    public HttpResponse postResponse(String url, String charset, Map<String, String> paramMap) throws Exception {
        Header[] headers = {};
        return this.postResponse(url, charset, paramMap, headers);
    }

    public HttpResponse postResponse(String url, String charset, Map<String, String> paramMap, Header[] headers) throws Exception {
        try {
            HttpPost post = new HttpPost(url);
            List<NameValuePair> nvps = this.converForMap(paramMap);
            if (null != headers && 0 < headers.length) {
                post.setHeaders(headers);
            }
            post.setEntity(new UrlEncodedFormEntity(nvps, charset));
            return httpClient.execute(post);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            throw e;
        } finally {
//			if (httpClient != null) {
//				httpClient.getConnectionManager().shutdown();
//			}
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
            _logger.info("status =" + status);
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
            _logger.info("status =" + status);
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

    public String doPost(String url, String charset, Map<String, String> paramMap, Header header) throws Exception {
        try {
            HttpPost post = new HttpPost(url);
            post.setHeader(header);
            post.setHeader("Host", "tyclub.telefen.com");
            post.setHeader("Connection", "keep-alive");
            post.setHeader("Accept", "image/webp,image/*,*/*;q=0.8");
            post.setHeader("User-Agent", "iphone");
            post.setHeader("Referer", "http://tyclub.telefen.com/newjf_hgo2/html/HGOIndex_em.html?provinceId=35");
            post.setHeader("Accept-Encoding", "gzip, deflate, sdch");
            post.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
            List<NameValuePair> nvps = converForMap(paramMap);
            post.setEntity(new UrlEncodedFormEntity(nvps, charset));
            HttpResponse httpResponse = httpClient.execute(post);
            int status = httpResponse.getStatusLine().getStatusCode();
            _logger.info("status =" + status);
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
            List<NameValuePair> nvps = converForMap(paramMap);
            post.setEntity(new UrlEncodedFormEntity(nvps, charset));
            HttpResponse httpResponse = client.execute(post);
            int status = httpResponse.getStatusLine().getStatusCode();
            _logger.info("status =" + status);
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
            s.setContentType("application/json");
            post.setEntity(s);
            post.addHeader("Content-Type", "application/json;charset=UTF-8");
            HttpResponse httpResponse = client.execute(post);
            int status = httpResponse.getStatusLine().getStatusCode();
            _logger.info("status =" + status);
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
