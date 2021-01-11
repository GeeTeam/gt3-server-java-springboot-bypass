package com.geetest.sdk;

import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

import com.geetest.sdk.utils.EncryptionUtils;
import com.geetest.sdk.utils.GeetestLogUtils;
import com.geetest.sdk.utils.RestTemplateUtils;

/*
 * sdk核心逻辑
 * 
 */
public class GeetestLib {
    
    public static boolean IS_DEBUG = true;  // 调试开关，是否输出调试日志
    
    private static String API_URL = "http://api.geetest.com";
    
    private static String REGISTER_URL = "/register.php";
    
    private static String VALIDATE_URL = "/validate.php";
    
    private static String REQUEST_REGISTER_URL = API_URL + REGISTER_URL;                     //Register请求url
    private static String REQUEST_VALIDATE_URL = API_URL + VALIDATE_URL;                     //Validate请求url
    
    private static String BYPASS_URL = "http://bypass.geetest.com/v1/bypass_status.php";      //bypass心跳检测url
//    private static String BYPASS_URL = "http://www.google.com";      //bypass心跳检测错误url  test

    private static String JSON_FORMAT = "1";
    
    private static boolean NEW_CAPTCHA = true ;
        
    private static String VERSION = "jave-servlet:3.1.1";
    
    public static String GEETEST_CHALLENGE = "geetest_challenge";  // 极验二次验证表单传参字段 chllenge
    
    public static String GEETEST_VALIDATE = "geetest_validate";   // 极验二次验证表单传参字段 validate
    
    public static String GEETEST_SECCODE = "geetest_seccode";     // 极验二次验证表单传参字段 seccode
    
    private static String GEETEST_SERVER_STATUS_SESSION_KEY = "gt_server_status";  // 如果使用缓存保存极验服务器状态，极验验证API服务状态Session Key
    
    
    private static String geetestId;                                                //用户id
    
    private static String geetestKey;                                               //用户key
    
    private static String digestmod = "md5";                                                    //加密方式
    
    
    
    
    
    public static void initUser(String geetestId, String geetestKey) { 
        /**
         * 初始化用户 id 和 key 值
         */
        GeetestLib.geetestId = geetestId;
        GeetestLib.geetestKey = geetestKey;
    }
    
    
    
    public static void setDigestmod(String digestmod) {
        /**
         * 设置加密模式， 支持 sha256，hmac_sha256，md5，默认为md5
         */
        if("sha256".equals(digestmod) || "hmac_sha256".equals(digestmod) || "md5".equals(digestmod))
            GeetestLib.digestmod = digestmod;
        else {
            GeetestLib.digestmod = "md5";
            GeetestLogUtils.log("setDigestmod(): 设置加密方式为 = " + GeetestLib.digestmod);
        }
    }
    
    
    
    
    public static boolean byPass() {
        /**
         * 注意：
         * byPass心跳检测, 在和极验服务器交互前，提前检测极验服务器的状态
         * 客户可自行设计缓存，如redis，启动定时任务，查询缓存到本地，保存极验服务器状态变量，减少网络请求耗时。
         */
        
        JSONObject jsonObject = null;
        try {
            jsonObject = RestTemplateUtils.doByPass(BYPASS_URL, geetestId);
            String status = jsonObject.getString("status");
            GeetestLogUtils.log("byPass()：心跳检测结果 = " + status);
            if("success".equals(status)) {
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            GeetestLogUtils.log("byPass()：心跳检测错误  = " + e.getMessage());
            return false;
        }       
    }
    
    
    
    
    
    public static GeetestLibResult localRegister() {
        /**
         * bypass（心跳检测），检测到极验宕机情况下，调用此方法，进入宕机模式，本地生成challeng号，容灾处理。
         */
        GeetestLibResult libResult = buildChallenge(null);
        GeetestLogUtils.log("localRegister(): bypass当前状态为fail，后续流程将进入宕机模式");
        return libResult;   
    }
    
    
    
    
    
    
    public static GeetestLibResult register(Map<String, String> paramMap) {
        /**
         * 验证初始化，paramMap：注册参数
         */
        if(byPass()==false)
            return localRegister();
        
        GeetestLogUtils.log(String.format(
                "register(): 开始验证初始化, digestmod=%s",REQUEST_REGISTER_URL ,paramMap.toString())
                );
        
        String origin_challenge = requestRegister(paramMap);
        GeetestLibResult libResult = buildChallenge(origin_challenge);
        GeetestLogUtils.log("register(): 验证初始化完成, GeetestLibResult对象返回信息 = %s"+libResult.toJSON());
        
        return libResult;
    }
    
    
    
    
    
    
    public static String requestRegister(Map<String, String> paramMap) {
        /**
         * 发送第一次请求
         */
        paramMap.put("digestmod", digestmod);
        paramMap.put("gt", geetestId);
        paramMap.put("sdk", VERSION);
        paramMap.put("json_format", JSON_FORMAT);
        
        GeetestLogUtils.log(String.format(
                "requestRegister(): 验证初始化, 向极验发送请求, url=%s, params=%s",REQUEST_REGISTER_URL ,paramMap.toString())
                );
        //极验返回的json数据
        JSONObject resBody = null;
        
        try {
            resBody = RestTemplateUtils.doGet(REQUEST_REGISTER_URL, paramMap);
        } catch (Exception e) {
            GeetestLogUtils.log("requestRegister(): 与极验网络交互异常，后续流程走宕机模式，" + e.getMessage());
            return null;
        }
        
        String origin_challenge = null;
        try {
            origin_challenge = resBody.getString("challenge");
        } catch (Exception e) {
            GeetestLogUtils.log("requestRegister():极验返回数据异常，后续流程走宕机模式");
            return null;
        }
    
        
        GeetestLogUtils.log("requestRegister()：请求返回challenge  = "+origin_challenge);
        
        return origin_challenge;
    }
    
    
    
    
    
    
    
    public static GeetestLibResult buildChallenge(String challenge) {
        /**
         *  对origin_challenge加密处理，并封装进GeetestResult对象。
         *origin_challenge为null或者值为0 代表请求极验服务失败，后续流程将进入宕机模式
         */     
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("gt", geetestId);
        jsonObject.put("new_captcha", NEW_CAPTCHA);
        
        if(challenge==null || "0".equals(challenge)) {
            challenge = getRandomString(32);                            //本地随机生成32位字符串
            jsonObject.put("challenge", challenge);
            jsonObject.put("success", 0);
            return new GeetestLibResult(0, jsonObject.toString(), "bypass当前状态为fail，后续流程走宕机模式");
        }
        
        challenge = EncryptionUtils.encode(challenge, geetestKey, digestmod);        //加密流程
        jsonObject.put("challenge", challenge);
        jsonObject.put("success", 1);
        
        return new GeetestLibResult(1, jsonObject.toString(), "");
        
    }
    
    
    
    
    
    
    
    public static GeetestLibResult validate(Map<String, String> validateParams) {
        /**
         * 准备和极验第二次交互，向极验请求Validate验证处理。
         */
        GeetestLogUtils.log("Validate():向极验请求Validate验证处理，params = "+validateParams);
        
        
        if(checkParams(validateParams)) {
            return new GeetestLibResult(0, "", "正常模式，本地校验，参数challenge、validate、seccode不可为空");
        }
        
        // geetest心跳检测，失败

        if(byPass()==false) {
            GeetestLogUtils.log("validate(): 二次验证 宕机模式，通过验证");
            return new GeetestLibResult(1, "", "");
        }
        


        // geetest心跳检测，成功
        // 对极验返回的二次校验结果进行处理，返回相应GeetestLibResult
        String responseSeccode = requestValidate(validateParams);
            
        if(responseSeccode == null)
            return new GeetestLibResult(0, "", "请求极验validate接口失败");
            
        if("false".equals(responseSeccode))
            return new GeetestLibResult(0, "", "极验二次验证不通过");
            
        return new GeetestLibResult(1, "", "");
            
    }
    
    
    
    
    
    
    public static String requestValidate(Map<String, String> validateParams) {
        /**
         * 向极验发送二次验证的请求，POST方式
         * 返回String类型：responseSeccode，极验二次验证结果。
         */
        
        validateParams.put("json_format", JSON_FORMAT);
        validateParams.put("sdk", VERSION);
        validateParams.put("captchaid", geetestId);
        
        GeetestLogUtils.log(String.format(
                "requestValidate(): 开始二次验证 正常模式, 向极验发送POST请求,进行校验 。url=%s , postParams=%s",
                REQUEST_VALIDATE_URL,
                validateParams)
                );
        
        JSONObject jsonObject = null;
        String responseSeccode = null;
        
        try {
            jsonObject = RestTemplateUtils.doPost(REQUEST_VALIDATE_URL, validateParams);
            GeetestLogUtils.log("requestValidate(): 二次验证 正常模式, 与极验网络交互正常,  返回body="+jsonObject);
            if(jsonObject != null)
                responseSeccode = jsonObject.getString("seccode");
            
        } catch (Exception e) {
            GeetestLogUtils.log("requestValidate(): 二次验证 正常模式, 请求异常,  " + e.getMessage());
            return null;
        }
        
        return responseSeccode;
        
    }
    
    
    
    
    
    public static boolean checkParams(Map<String, String> validateParams)   {
        /**
         * 二次验证的POST参数校验 
         */
        String challenge = validateParams.get("challenge");
        String validate = validateParams.get("validate");
        String seccode = validateParams.get("seccode");
        
        if(challenge == null || challenge.isEmpty() 
            || validate == null || validate.isEmpty()
            || seccode == null || seccode.isEmpty() ) {
            return true;
        }
        return false;
    }
    
    
    
    
    
    public static Random random = new Random();
    
    public static String getRandomString(int length) {
        /**
         * 生成随机32位字符串，当做宕机模式下的challeng号
         */ 
        String str="abcdefghijklmnopqrstuvwxyz0123456789";
//      Random random = new Random();
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number=random.nextInt(str.length());   
            char charAt = str.charAt(number);
            sb.append(charAt);
          }
          
        return sb.toString();   
    }
    
    
}
