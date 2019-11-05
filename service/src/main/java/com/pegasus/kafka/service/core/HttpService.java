package com.pegasus.kafka.service.core;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HttpService {

    private static final Integer CONNECTION_TIMEOUT = 5000;
    private static final Integer REQUEST_TIMEOUT = 5000;
    private static final Integer SOCKET_TIMEOUT = 5000;

    public String get(String url) throws ConnectTimeoutException {
        if (url.toLowerCase().startsWith("https:")) {
            return get(url, null, null, true);
        } else {
            return get(url, null, null, false);
        }
    }

    public String get(String url,
                      Map<String, Object> params) throws ConnectTimeoutException {
        if (url.toLowerCase().startsWith("https:")) {
            return get(url, params, null, true);
        } else {
            return get(url, params, null, false);
        }
    }

    public String get(String url,
                      Map<String, Object> params,
                      Map<String, String> headers) throws ConnectTimeoutException {
        if (url.toLowerCase().startsWith("https:")) {
            return get(url, params, headers, true);
        } else {
            return get(url, params, headers, false);
        }
    }

    public String post(String url,
                       Map<String, Object> params) throws ConnectTimeoutException {
        if (url.toLowerCase().startsWith("https:")) {
            return post(url, params, null, true);
        } else {
            return post(url, params, null, false);
        }
    }

    public String post(String url,
                       Map<String, Object> params,
                       Map<String, String> headers) throws ConnectTimeoutException {
        if (url.toLowerCase().startsWith("https:")) {
            return post(url, params, headers, true);
        } else {
            return post(url, params, headers, false);
        }
    }

    private String get(String url,
                       @Nullable Map<String, Object> params,
                       @Nullable Map<String, String> headers,
                       boolean isHttps) throws ConnectTimeoutException {
        String strResult;
        CloseableHttpClient httpClient;
        if (isHttps) {
            httpClient = createSSLClientDefault();
        } else {
            httpClient = HttpClients.createDefault();
        }
        try {
            StringBuilder strParams = new StringBuilder();
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, Object> pair : params.entrySet()) {
                    strParams.append(String.format("%s=%s&", pair.getKey(), pair.getValue().toString()));
                }
                url = String.format("%s?%s", url, strParams.substring(0, strParams.length() - 1));
            }
            HttpGet request = new HttpGet(url);
            if (null != headers && headers.size() > 0) {
                List<Header> _headers = new ArrayList<>(headers.size());
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    _headers.add(new BasicHeader(header.getKey(), header.getValue()));
                }
                request.setHeaders(_headers.toArray(new Header[]{}));
            }
            RequestConfig requestConfig = RequestConfig // 配置
                    .custom() // 开启自定义模式
                    .setConnectTimeout(CONNECTION_TIMEOUT) // 设置超时连接超时时间
                    .setConnectionRequestTimeout(REQUEST_TIMEOUT) // 设置连接后的请求处理超时时间
                    .setSocketTimeout(SOCKET_TIMEOUT) // 设置整体socket的超时时间
                    .build(); // 构建
            request.setConfig(requestConfig);
            HttpResponse response = httpClient.execute(request);
            strResult = EntityUtils.toString(response.getEntity());
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (Exception ignored) {
            }
        }
        return strResult;
    }

    private String post(String url,
                        @Nullable Map<String, Object> params,
                        @Nullable Map<String, String> headers,
                        boolean isHttps) throws ConnectTimeoutException {
        String strResult;
        CloseableHttpClient httpClient;
        if (isHttps) {
            httpClient = createSSLClientDefault();
        } else {
            httpClient = HttpClients.createDefault();
        }
        try {
            HttpPost request = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig // 配置
                    .custom() // 开启自定义模式
                    .setConnectTimeout(CONNECTION_TIMEOUT) // 设置超时连接超时时间
                    .setConnectionRequestTimeout(REQUEST_TIMEOUT) // 设置连接后的请求处理超时时间
                    .setSocketTimeout(SOCKET_TIMEOUT) // 设置整体socket的超时时间
                    .build(); // 构建
            request.setConfig(requestConfig);
            if (null != params && params.size() > 0) {
                List<NameValuePair> _params = new ArrayList<>(params.size());
                for (Map.Entry<String, Object> pair : params.entrySet()) {
                    _params.add(new BasicNameValuePair(pair.getKey(), pair.getValue().toString()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(_params, "utf-8");
                entity.setContentType("application/x-www-form-urlencoded");
                entity.setContentEncoding("UTF-8");
                request.setEntity(entity);
            }
            if (null != headers && headers.size() > 0) {
                List<Header> _headers = new ArrayList<>(headers.size());
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    _headers.add(new BasicHeader(header.getKey(), header.getValue()));
                }
                request.setHeaders(_headers.toArray(new Header[]{}));
            }
            HttpResponse result = httpClient.execute(request);
            strResult = EntityUtils.toString(result.getEntity());
        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException ignored) {
            }
        }
        return strResult;
    }

    public JSONObject getJSON(String url,
                              Map<String, Object> parameters) throws Exception {
        return new JSONObject(get(url, parameters));
    }

    public JSONObject postJSON(String url,
                               Map<String, Object> parameters) throws Exception {
        return new JSONObject(post(url, parameters));
    }

    private CloseableHttpClient createSSLClientDefault() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> {
                return true; // 本地验证直接通过
            }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }

}
