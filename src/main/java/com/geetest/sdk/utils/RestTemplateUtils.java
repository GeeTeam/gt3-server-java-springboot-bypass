package com.geetest.sdk.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import com.google.common.base.Joiner;


public class RestTemplateUtils {


    
    public static JSONObject doGet(String url, Map<String, String> paramMap) throws Exception{
        /**
         * 向极验服务器发送get请求, 返回JSONObject对象。
         */
        RestTemplate restTemplate = RestTemplateFactory.getHttpRestTemplate();
        //构造GET请求url
        String request_url = url  + asUrlParams(paramMap);
        
        String resBody = restTemplate.getForObject(request_url, String.class);
        
        return new JSONObject(resBody);
    }
    
    
    
    
    public static JSONObject doPost(String url, Map<String, String> paramMap) throws Exception{
        /**
         * 向极验服务器发送post请求
         */
        RestTemplate restTemplate = RestTemplateFactory.getHttpRestTemplate();
        
        MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<>();   
        multiValueMap.add("challenge", paramMap.get("challenge"));
        multiValueMap.add("sdk", paramMap.get("sdk"));
        multiValueMap.add("captchaid", paramMap.get("captchaid"));
        multiValueMap.add("json_format", paramMap.get("json_format"));
        multiValueMap.add("client_type", paramMap.get("client_type"));
        multiValueMap.add("seccode", paramMap.get("seccode"));
        multiValueMap.add("user_id", paramMap.get("user_id"));
        multiValueMap.add("ip_address", paramMap.get("ip_address"));

        HttpEntity<MultiValueMap<String,String>> request = RestTemplateFactory.getHttpEntity(multiValueMap);
        
        //发送POST请求
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        String resBody = response.getBody();

        return new JSONObject(resBody);
    }
    
    
    
    
    public static JSONObject doByPass(String byPassUrl, String geetestId) throws Exception{
        /**
         * 向极验服务器发送get请求, byPass心跳检测。
         * byPassUrl: 心跳检测接口
         * geetestId: 客户geetestId
         */
        RestTemplate restTemplate = RestTemplateFactory.getHttpRestTemplate();
        byPassUrl = byPassUrl + "?gt=" + geetestId; 
        String resBody = restTemplate.getForObject(byPassUrl, String.class);
        
        return new JSONObject(resBody);
    }



    
    public static String asUrlParams(Map<String, String> paramMap) throws UnsupportedEncodingException{
        /**
         * map参数拼接，转成GET请求的路由（value使用url编码）
         */
        StringBuffer sb = new StringBuffer("?");
        Iterator<Map.Entry<String, String>> entries = paramMap.entrySet().iterator();
        
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            sb.append(entry.getKey());
            sb.append("=");
            //对GET请求的value值进行url编码
            sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            if(entries.hasNext())
                sb.append("&");         
        }
        GeetestLogUtils.log(sb.toString());
        return sb.toString();
    }

}
