package com.geetest.demo;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.geetest.sdk.GeetestLib;
import com.geetest.sdk.GeetestLibResult;
import com.geetest.sdk.utils.GeetestLogUtils;

@RestController
public class IndexController {
    private static String geetestId = "df5c92564f4d055a8e38e94183469f6f";
    private static String geetestKey = "0c4d6988ddc761e737a23fddf51acb65";
    
    static { 
        /*
         * Geetest服务初始化
         */
        GeetestLib.initUser(geetestId, geetestKey);
        GeetestLib.setDigestmod("md5");
    }
    
    
    
    
    @RequestMapping(value="/",method={RequestMethod.GET})
    public ModelAndView index() {
        return new ModelAndView("index");   
    }
    
    
    
    @RequestMapping(value="/favicon.ico",method={RequestMethod.GET})
    public String favicon() {
        // 图标请求
        return "favicon.ico";   
    }
    
    
    
    
    
    @RequestMapping(value="/register",method={RequestMethod.GET})
    public String firstRegister() {
        /**
         * 第一次验证注册请求
         */ 
        Map<String, String> paramMap = new HashMap<String, String>();
        //     user_id 客户端用户的唯一标识，确定用户的唯一性；作用于提供进阶数据分析服务，可在register和validate接口传入，不传入也不影响验证服务的使用；若担心用户信息风险，可作预处理(如哈希处理)再提供到极验
        paramMap.put("user_id", "test");
        paramMap.put("client_type", "web");
        paramMap.put("ip_address", "127.0.0.1");

        GeetestLibResult result =  GeetestLib.register(paramMap);
        return result.getData();
    
    }
    
    
    
    @RequestMapping(value="/validate",method={RequestMethod.POST})
    public String secondValidate(@RequestParam Map<String,Object> postParams) {
        /**
         * 第二次验证请求： postParams：POST数据
         * challenge, validate, seccode。进行二次校验时，前端POST提交的时候传回的三个参数
         */
        String challenge = (String) postParams.get(GeetestLib.GEETEST_CHALLENGE);
        String validate = (String) postParams.get(GeetestLib.GEETEST_VALIDATE);
        String seccode = (String) postParams.get(GeetestLib.GEETEST_SECCODE);
        
        //     user_id = session.get("user_id");
        //     client_type 客户端类型，web：电脑上的浏览器；h5：手机上的浏览器，包括移动应用内完全内置的web_view；native：通过原生sdk植入app应用的方式；unknown：未知
        //     ip_address 客户端请求sdk服务器的ip地址
        
        //      validateParams:向极验提供的校验数据
        Map<String,String> validateParams = new HashMap<String, String>();
        validateParams.put("challenge", challenge);
        validateParams.put("validate", validate);
        validateParams.put("seccode", seccode);
        validateParams.put("user_id", "user_id");
        validateParams.put("client_type", "web");
        validateParams.put("ip_address", "127.0.0.1");

        
        GeetestLibResult result = GeetestLib.validate(validateParams);
        
        JSONObject jsonObject = new JSONObject();
        
        if(result.getStatus()==1) {
            jsonObject.put("result", "success");
            jsonObject.put("version", "jave-servlet:3.1.1");            
        }else {
            jsonObject.put("msg", result.getMsg());
            jsonObject.put("result", "fail");
            jsonObject.put("version", "jave-servlet:3.1.1");
        }

        return jsonObject.toString();   
    }

}
