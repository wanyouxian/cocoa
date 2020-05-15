package com.rocky.cocoa.core.util;

import cn.hutool.json.JSONUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

public class StatefulHttpClient {

  private HttpClientContext context;
  private CloseableHttpClient httpclient;
  private int requestTimeOut = 15;
  private int sessionTimeOut = 60;

  public StatefulHttpClient(int sessionTimeOut, int requestTimeOut,
                            HttpHost proxy) {
    initCookieStore();
    this.sessionTimeOut = sessionTimeOut;
    this.requestTimeOut = requestTimeOut;
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
        .setConnectTimeout(this.requestTimeOut * 1000).setSocketTimeout(this.requestTimeOut * 1000);
    if (proxy != null) {
      requestConfigBuilder.setProxy(proxy);
    }
    httpclient = HttpClientBuilder.create()
        .setDefaultRequestConfig(requestConfigBuilder.build()).build();
  }

  public StatefulHttpClient(HttpHost proxy) {
    initCookieStore();
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
        .setConnectTimeout(10000).setSocketTimeout(this.requestTimeOut * 1000);
    if (proxy != null) {
      requestConfigBuilder.setProxy(proxy);
    }
    httpclient = HttpClientBuilder.create()
        .setDefaultRequestConfig(requestConfigBuilder.build()).build();
  }

  public int getRequestTimeOut() {
    return requestTimeOut;
  }

  public void setRequestTimeOut(int requestTimeOut) {
    this.requestTimeOut = requestTimeOut;
  }

  public int getSessionTimeOut() {
    return sessionTimeOut;
  }

  public void setSessionTimeOut(int sessionTimeOut) {
    this.sessionTimeOut = sessionTimeOut;
  }

  private void initCookieStore() {
    PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
    Registry<CookieSpecProvider> cookieSpecReg = RegistryBuilder.<CookieSpecProvider>create()
        .register(CookieSpecs.DEFAULT, new DefaultCookieSpecProvider(publicSuffixMatcher))
        .register(CookieSpecs.STANDARD, new RFC6265CookieSpecProvider(publicSuffixMatcher)).build();
    CookieStore cookieStore = new BasicCookieStore();

    context = HttpClientContext.create();
    context.setCookieSpecRegistry(cookieSpecReg);
    context.setCookieStore(cookieStore);
  }


  /**
   * http get.
   *
   * @param clazz clazz
   * @param url url
   * @param queryParam queryParam
   * @param headers headers
   * @param <T> T
   * @return T
   * @throws IOException e
   */
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> clazz, String url,
      Map<String, String> queryParam, Map<String, String> headers) throws IOException {
    HttpGet get = new HttpGet(url);
    if (headers != null && headers.size() > 0) {
      for (String key : headers.keySet()) {
        get.setHeader(key, headers.get(key));
      }
    }
    try {
      if (queryParam != null && queryParam.size() > 0) {
        URIBuilder builder = new URIBuilder(get.getURI());
        for (String key : queryParam.keySet()) {
          builder.addParameter(key, queryParam.get(key));
        }
        get.setURI(builder.build());
      }

    } catch (Exception exception) {
      exception.printStackTrace();

    }
    HttpEntity entity = null;
    try {
      HttpResponse response = httpclient.execute(get, context);
      int statusCode = response.getStatusLine().getStatusCode() / 100;
      entity = response.getEntity();
      String result = EntityUtils.toString(response.getEntity());
      if (statusCode == 4 || statusCode == 5) {
        throw new IOException(result);
      }

      if (String.class.equals(clazz)) {
        return (T) result;
      }
      if (result == null || result.length() == 0) {
        return null;
      }
      return JSONUtil.toBean(result, clazz);
    } finally {
      if (entity != null) {
        EntityUtils.consume(entity);
      }
    }
  }

  /**
   * http post.
   *
   * @param clazz clazz
   * @param url url
   * @param queryParam queryParam
   * @param headers headers
   * @param <T> T
   * @return T
   * @throws IOException e
   */
  @SuppressWarnings("unchecked")
  public <T> T post(Class<T> clazz, String url,
      Map<String, String> queryParam, Map<String, String> headers,
      String body) throws IOException {

    HttpPost post = new HttpPost(url);
    if (headers != null && headers.size() > 0) {
      for (String key : headers.keySet()) {
        post.setHeader(key, headers.get(key));
      }
    }
    try {
      if (queryParam != null && queryParam.size() > 0) {
        URIBuilder builder = new URIBuilder(post.getURI());
        for (String key : queryParam.keySet()) {
          builder.addParameter(key, queryParam.get(key));
        }
        post.setURI(builder.build());
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }

    if (body != null) {
      HttpEntity entity = new StringEntity(body,
          ContentType.APPLICATION_JSON);
      post.setEntity(entity);
    }
    HttpEntity entity = null;
    try {
      HttpResponse response = httpclient.execute(post, context);
      int statusCode = response.getStatusLine().getStatusCode() / 100;
      entity = response.getEntity();
      String result = EntityUtils.toString(response.getEntity());
      if (statusCode == 4 || statusCode == 5) {
        throw new IOException(result);
      }
      if (Void.class.equals(clazz)) {
        return null;
      }
      if (String.class.equals(clazz)) {
        return (T) result;
      }
      if (result == null || result.length() == 0) {
        return null;
      }
      return JSONUtil.toBean(result, clazz);
    } finally {
      if (entity != null) {
        EntityUtils.consume(entity);
      }
    }
  }

  /**
   * http upload.
   *
   * @param clazz clazz
   * @param url url
   * @param queryParam queryParam
   * @param headers headers
   * @param filePaths filepaths
   * @param <T> T
   * @return T
   * @throws IOException e
   */
  @SuppressWarnings("unchecked")
  public <T> T upload(Class<T> clazz, String url,
      Map<String, String> queryParam, Map<String, String> headers, List<String> filePaths)
      throws IOException {

    HttpPost post = new HttpPost(url);
    if (headers != null && headers.size() > 0) {
      for (String key : headers.keySet()) {
        post.setHeader(key, headers.get(key));
      }
    }
    try {
      if (queryParam != null && queryParam.size() > 0) {
        URIBuilder builder = new URIBuilder(post.getURI());
        for (String key : queryParam.keySet()) {
          builder.addParameter(key, queryParam.get(key));
        }
        post.setURI(builder.build());
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }

    if (filePaths != null) {
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
      for (String path : filePaths) {
        File file = new File(path);
        builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
      }
      HttpEntity entity = builder.build();
      post.setEntity(entity);
    }

    HttpEntity entity = null;
    try {
      HttpResponse response = httpclient.execute(post, context);
      int statusCode = response.getStatusLine().getStatusCode() / 100;
      entity = response.getEntity();
      String result = EntityUtils.toString(response.getEntity());
      if (statusCode == 4 || statusCode == 5) {
        throw new IOException(result);
      }
      if (String.class.equals(clazz)) {
        return (T) result;
      }
      if (result == null || result.length() == 0) {
        return null;
      }
      return JSONUtil.toBean(result, clazz);
    } finally {
      if (entity != null) {
        EntityUtils.consume(entity);
      }
    }
  }

  /**
   * http download.
   *
   * @param url url
   * @param queryParam queryParam
   * @param headers headers
   * @param filePath filepath
   * @throws IOException e
   */
  @SuppressWarnings("unchecked")
  public void download(String url,
      Map<String, String> queryParam, Map<String, String> headers, String filePath)
      throws IOException {

    HttpPost post = new HttpPost(url);
    if (headers != null && headers.size() > 0) {
      for (String key : headers.keySet()) {
        post.setHeader(key, headers.get(key));
      }
    }
    try {
      if (queryParam != null && queryParam.size() > 0) {
        URIBuilder builder = new URIBuilder(post.getURI());
        for (String key : queryParam.keySet()) {
          builder.addParameter(key, queryParam.get(key));
        }
        post.setURI(builder.build());
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }

    HttpEntity entity = null;
    try {
      HttpResponse response = httpclient.execute(post, context);
      int statusCode = response.getStatusLine().getStatusCode() / 100;
      entity = response.getEntity();
      if (statusCode == 4 || statusCode == 5) {
        throw new IOException("");
      }
      BufferedInputStream bis = new BufferedInputStream(entity.getContent());
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
      int inByte;
      while ((inByte = bis.read()) != -1) {
        bos.write(inByte);
      }
      bis.close();
      bos.close();
    } finally {
      if (entity != null) {
        EntityUtils.consume(entity);
      }
    }
  }

  /**
   * http downloadzip.
   *
   * @param url url
   * @param queryParam queryParam
   * @param headers headers
   * @param body body
   * @param filePath filepath
   * @throws IOException e
   */
  @SuppressWarnings("unchecked")
  public void downloadzip(String url,
      Map<String, String> queryParam, Map<String, String> headers, String body, String filePath)
      throws IOException {

    HttpPost post = new HttpPost(url);
    if (headers != null && headers.size() > 0) {
      for (String key : headers.keySet()) {
        post.setHeader(key, headers.get(key));
      }
    }
    try {
      if (queryParam != null && queryParam.size() > 0) {
        URIBuilder builder = new URIBuilder(post.getURI());
        for (String key : queryParam.keySet()) {
          builder.addParameter(key, queryParam.get(key));
        }
        post.setURI(builder.build());
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    if (body != null) {
      HttpEntity entity = new StringEntity(body,
          ContentType.APPLICATION_JSON);
      post.setEntity(entity);
    }
    HttpEntity entity = null;
    try {
      HttpResponse response = httpclient.execute(post, context);
      int statusCode = response.getStatusLine().getStatusCode() / 100;
      entity = response.getEntity();
      if (statusCode == 4 || statusCode == 5) {
        throw new IOException("");
      }
      BufferedInputStream bis = new BufferedInputStream(entity.getContent());
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
      int inByte;
      while ((inByte = bis.read()) != -1) {
        bos.write(inByte);
      }
      bis.close();
      bos.close();
    } finally {
      if (entity != null) {
        EntityUtils.consume(entity);
      }
    }
  }
}
