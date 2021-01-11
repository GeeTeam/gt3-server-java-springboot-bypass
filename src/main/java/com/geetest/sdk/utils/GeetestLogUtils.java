package com.geetest.sdk.utils;

import com.geetest.sdk.GeetestLib;

/*
 * debug log打印工具类
 */
public class GeetestLogUtils {
    
    public static void log(String mes) {
        if(GeetestLib.IS_DEBUG) {
            System.out.println();
            System.out.println("--------------------------------------------------------");
            System.out.println("geetestLog: "+ mes);
            System.out.println("--------------------------------------------------------");
            System.out.println();
        }
        
    }
}
