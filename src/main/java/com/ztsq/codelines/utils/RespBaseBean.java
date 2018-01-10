package com.ztsq.codelines.utils;

/**
 * Created by zyuework on 2018/1/10.
 */
public class RespBaseBean {
    private int resultCode;
    private String msg;
    private Object data;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


    public static RespBaseBean createSuccessResp(Object dataObj){
        RespBaseBean bean = new RespBaseBean();
        bean.setResultCode(1);
        bean.setMsg("查询成功");
        bean.setData(dataObj);
        return bean;
    }

    public static RespBaseBean createErrorResp(int code,String errMsg){
        RespBaseBean bean = new RespBaseBean();
        if (code == 1){
            throw  new IllegalArgumentException("code 不能设置为 1");
        }

        bean.setResultCode(code);
        bean.setMsg(errMsg);
        bean.setData("");
        return bean;
    }
}
