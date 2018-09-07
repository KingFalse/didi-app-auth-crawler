package me.kagura.crawler.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class ResponseBuilder {
    public static String RespInit(String type) {
        JSONObject result = new JSONObject();
        result.put("next", type);//init|sms|captcha|password|success
        return result.toJSONString();
    }

    public static String RespSuccess(String userInfo) {
        JSONObject result = new JSONObject();
        result.put("next", "success");//init|sms|captcha|password|success
        result.put("userinfo", JSON.parseObject(userInfo));
        return result.toJSONString();
    }

    public static String RespInit(String type, String captchaBase64) {
        JSONObject result = new JSONObject();
        result.put("next", type);//init|sms|captcha|password|success
        result.put("captchaBase64", captchaBase64);
        return result.toJSONString();
    }

    public static String RespInit(String type, String captchaBase64, String errmsg) {
        JSONObject result = new JSONObject();
        result.put("next", type);//init|sms|captcha|password|success
        result.put("captchaBase64", captchaBase64);
        result.put("errmsg", errmsg);
        return result.toJSONString();
    }
}
