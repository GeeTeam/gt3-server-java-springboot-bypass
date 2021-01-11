package com.geetest.sdk.utils;

import java.nio.charset.StandardCharsets;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
 
/**
 * 极验http请求配置
 * demo没有使用到bean，有需要可自行配置
 */

public class RestTemplateFactory {
    
     private static final int MAX_TOTAL = 200; // 连接池最大连接数
     private static final int MAX_PER_ROUTE = 200; // 每个路由的最大连接数
     private static final int SOCKET_TIMEOUT = 5000; // 单位：毫秒
     private static final int CONNECT_TIMEOUT = 5000; // 单位：毫秒
//   private static final int RETRY_TIMES = 5; // 出错重置次数

     private static HttpHeaders headers = new HttpHeaders();                        //二次校验请求POST请求头

     
     private static ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
             restTemplateConfigHttpClient());
     
    
     public static RestTemplate getHttpRestTemplate() {
         /**
          * 工厂方法，获得RestTemplate实例对象
          */
        RestTemplate restTemplate = new RestTemplate(factory);
        // 可以添加消息转换
        //restTemplate.setMessageConverters(...);
        // 可以增加拦截器
        //restTemplate.setInterceptors(...);
        //设置编码格式
        restTemplate.getMessageConverters().set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
     
     
     
     
     public static HttpEntity<MultiValueMap<String,String>> getHttpEntity(MultiValueMap<String,String> multiValueMap) {
         /**
          * 获得HttpEntity实例对象
          */
        return new HttpEntity<MultiValueMap<String,String>>(multiValueMap, headers);
    }
 
    
    
       
 
    public static HttpClient restTemplateConfigHttpClient() {
        /**
         * 连接配置
         */
        // 设置请求头
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        // 设置整个连接池最大连接数 根据自己的场景决定
        // todo 后面调整从配置中心获取
        connectionManager.setMaxTotal(MAX_TOTAL);
        // 路由是对maxTotal的细分
        // todo 后面调整从配置中心获取
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        RequestConfig requestConfig = RequestConfig.custom()
                // 服务器返回数据(response)的时间，超过该时间抛出read timeout
                // todo 后面调整从配置中心获取
                .setSocketTimeout(SOCKET_TIMEOUT)
                // 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
                // todo 后面调整从配置中心获取
                .setConnectTimeout(SOCKET_TIMEOUT)
                // 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，
                // 会抛出org.apache.http.conn.ConnectionPoolTimeoutException:
                // Timeout waiting for connection from pool
                // todo 后面调整从配置中心获取
                .setConnectionRequestTimeout(CONNECT_TIMEOUT)
                .build();
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }
}