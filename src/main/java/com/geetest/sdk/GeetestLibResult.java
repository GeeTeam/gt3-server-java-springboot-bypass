package com.geetest.sdk;

/*
 * 和Geetest服务器交互返回结果，封装类 
 */
public class GeetestLibResult {
/**
* @param status: 成功失败的标识码，1表示成功，0表示失败
* @param data: 返回数据，json格式 
* @param msg: 备注信息，如异常信息等
*/

    private int status = 0;
    private String data = "";
    private String msg = "";
    
    

    public String getMsg() {
        return msg;
    }


    public int getStatus() {
        return status;
    }


    public String getData() {
        return data;
    }


    public GeetestLibResult(int status, String data, String msg) {
        this.status = status;
        this.data = data;
        this.msg = msg;
    }
    
    
    public void setAll(int status, String data, String msg) {
        this.status = status;
        this.data = data;
        this.msg = msg;
    }
    

    public String toJSON() {
        // 返回Json数据格式
        return String.format("{'status':%s, 'data':%s, 'msg': %s}", status, data, msg);
    }

}
