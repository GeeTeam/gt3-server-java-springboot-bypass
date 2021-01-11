package com.geetest.sdk.utils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;


/**
 * challenge 和 geetest_key加密处理工具类
 *
 */
public class EncryptionUtils {
    
/** 
 * @param challenge   challenge号
 * @param geetest_key 极验Key
 * @param digestmod   加密方式
 * @return
 */
    public static String encode(String challenge, String geetest_key, String digestmod) {
        
        if("md5".equals(digestmod)) {
            return md5_encode(challenge + geetest_key); 
            
        } else if ("sha256".equals(digestmod)) {
            return sha256_encode(challenge + geetest_key);
            
        } else if ("hmac-sha256".equals(digestmod)) {
            return hmac_sha256_encode(challenge, geetest_key);
            
        }
        return  md5_encode(challenge + geetest_key);
    }
    
    
    
    public static String md5_encode(String challenge) {
        return DigestUtils.md5Hex(challenge);
    }
    
    
    
    public static String  sha256_encode(String challenge) {
        return DigestUtils.sha256Hex(challenge);
    }
    
    
    
    public static String  hmac_sha256_encode(String challenge, String geetest_key) {
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, geetest_key).hmacHex(challenge);
    }
}