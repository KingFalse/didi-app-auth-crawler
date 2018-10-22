package me.kagura.crawler.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import me.kagura.JJsoup;
import me.kagura.LoginInfo;
import me.kagura.crawler.utils.ResponseBuilder;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

@Service
public class AuthService {

    @Autowired
    JJsoup jJsoup;

    public String init(LoginInfo loginInfo) throws Exception {
        JSONObject request = new JSONObject();
        request.put("cell", loginInfo.getExtra("account"));
        request.put("appversion", "5.2.22");
        request.put("api_version", "1.0.1");
        request.put("appid", 10000);
        request.put("canonical_country_code", "CN");
        request.put("channel", "73399");
        request.put("city_id", -1);
        request.put("country_calling_code", "+86");
        request.put("country_id", 156);
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", 40.0434172675);//经度
        request.put("lng", 116.2904349353);//纬度
        request.put("map_type", "soso");
        request.put("model", "ONEPLUS A6000");
        request.put("network_type", "WIFI");
        request.put("os", "9");
        request.put("role", -1);
        request.put("scene", 0);
        request.put("suuid", loginInfo.getExtra("suuid"));
        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v5/gatekeeper", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/9 didihttp OneNet/2.1.0.66 com.sdu.didi.psnger/5.2.22");
                    put("Connection", "Keep-Alive");
                    put("Productid", "0");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "0");
                    put("TripCountry", "CN");
                    put("didi-header-hint-content", "{\"app_timeout_ms\":20000,\"Cityid\":-1,\"lang\":\"zh-CN\",\"utc_offset\":\"480\"}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .data(new HashMap<String, String>() {{
                    put("q", request.toJSONString());
                }})
                .execute();
        //新返回 {"errno":0,"error":"成功","requestid":"1538986865097559081","time":"2018-10-08 16:21:05","credential":"身份证","close_voice":0,"roles":[{"id":1,"login_type":2,"text":""}],"usertype":0}
        //{"errno":-103,"error":"手机号错误"}
        if (0 != (int) JSONPath.read(response.body(), "$.errno")) {
            return ResponseBuilder.RespInit("init", null, JSONPath.read(response.body(), "$.error") + "");
        }
        String result = "";
        int login_type = (int) JSONPath.read(response.body(), "$.roles[0].login_type");
        switch (login_type) {
            case 1://需要短信
                result = sendSMS(loginInfo);
                break;
            case 2://需要密码
                result = ResponseBuilder.RespInit("password");
                break;
        }
        return result;

    }

    /**
     * 发短信，如果返回需要图片验证码则走获取图片
     *
     * @param loginInfo
     * @return
     */
    public String sendSMS(LoginInfo loginInfo) throws Exception {
        JSONObject request = new JSONObject();
        request.put("cell", loginInfo.getExtra("account"));
        request.put("code_type", 0);
        request.put("appversion", "5.2.22");
        request.put("api_version", "1.0.1");
        request.put("appid", 10000);
        request.put("canonical_country_code", "CN");
        request.put("channel", "73399");
        request.put("city_id", -1);
        request.put("country_calling_code", "+86");
        request.put("country_id", 156);
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", 40.0434172675);//经度
        request.put("lng", 116.2904349353);//纬度
        request.put("map_type", "soso");
        request.put("model", "ONEPLUS A6000");
        request.put("network_type", "WIFI");
        request.put("os", "9");
        request.put("role", -1);
        request.put("scene", 0);
        request.put("suuid", loginInfo.getExtra("suuid"));

        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v5/codeMT", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/9 didihttp OneNet/2.1.0.66 com.sdu.didi.psnger/5.2.22");
                    put("Connection", "Keep-Alive");
                    put("Productid", "0");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "0");
                    put("TripCountry", "CN");
                    put("didi-header-hint-content", "{\"app_timeout_ms\":20000,\"Cityid\":-1,\"lang\":\"zh-CN\",\"utc_offset\":\"480\"}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .data(new HashMap<String, String>() {{
                    put("q", request.toJSONString());
                }})
                .execute();

        //{"errno":0,"error":"成功","requestid":"1538990957342689893","time":"2018-10-08 17:29:17","code_type":0,"data":{"code_len":6,"code_tag":"【滴滴出行】"},"support_voice":true}
        //{"errno":41002,"error":"请输入图形验证码","requestid":"1538992202084491993","time":"2018-10-08 17:50:02"}
        System.err.println("短信发送返回：" + response.body());
        int errno = (int) JSONPath.read(response.body(), "$.errno");
        switch (errno) {
            case 0://发送成功
                return ResponseBuilder.RespInit("sms");
            case 41002://需要图片
                loginInfo.extras.put("next", "sms");
                return getCaptcha(loginInfo);
        }
        return ResponseBuilder.RespInit("init", "", JSONPath.read(response.body(), "$.error") + "");
    }

    public String getCaptcha(LoginInfo loginInfo) throws Exception {
        JSONObject request = new JSONObject();
        request.put("cell", loginInfo.getExtra("account"));
        request.put("appversion", "5.2.22");
        request.put("api_version", "1.0.1");
        request.put("appid", 10000);
        request.put("canonical_country_code", "CN");
        request.put("channel", "73399");
        request.put("city_id", -1);
        request.put("country_calling_code", "+86");
        request.put("country_id", 156);
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", 40.0434172675);//经度
        request.put("lng", 116.2904349353);//纬度
        request.put("map_type", "soso");
        request.put("model", "ONEPLUS A6000");
        request.put("network_type", "WIFI");
        request.put("os", "9");
        request.put("role", 1);
        request.put("scene", -1);
        request.put("suuid", loginInfo.getExtra("suuid"));
        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v5/getCaptcha", loginInfo)
                .method(Connection.Method.POST)
                .data("q", request.toJSONString())
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/9 didihttp OneNet/2.1.0.66 com.sdu.didi.psnger/5.2.22");
                    put("Connection", "Keep-Alive");
                    put("Productid", "0");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "0");
                    put("TripCountry", "CN");
                    put("didi-header-hint-content", "{\"app_timeout_ms\":20000,\"Cityid\":-1,\"lang\":\"zh-CN\",\"utc_offset\":\"480\"}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .execute();
        //{"errno":53001,"error":"操作频繁，请您稍后再试","requestid":"1538992188699068104","time":"2018-10-08 17:49:48"}
        //{"errno":0,"error":"成功","requestid":"1538992186134403738","time":"2018-10-08 17:49:46","captcha":"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"}
        if ((int) JSONPath.read(response.body(), "$.errno") == 0) {
            return ResponseBuilder.RespInit("captcha", (String) JSONPath.read(response.body(), "$.captcha"));
        }
        return ResponseBuilder.RespInit("init", null, (String) JSONPath.read(response.body(), "$.error"));
    }

    public String captchaVerify(LoginInfo loginInfo) throws Exception {
        JSONObject request = new JSONObject();
        request.put("cell", loginInfo.getExtra("account"));
        request.put("captcha_code", loginInfo.getExtra("captcha"));
        request.put("appversion", "5.2.22");
        request.put("api_version", "1.0.1");
        request.put("appid", 10000);
        request.put("canonical_country_code", "CN");
        request.put("channel", "73399");
        request.put("city_id", -1);
        request.put("country_calling_code", "+86");
        request.put("country_id", 156);
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", 40.0434172675);//经度
        request.put("lng", 116.2904349353);//纬度
        request.put("map_type", "soso");
        request.put("model", "ONEPLUS A6000");
        request.put("network_type", "WIFI");
        request.put("os", "9");
        request.put("role", 1);
        request.put("scene", 1);
        request.put("suuid", loginInfo.getExtra("suuid"));

        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v5/verifyCaptcha", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/9 didihttp OneNet/2.1.0.66 com.sdu.didi.psnger/5.2.22");
                    put("Connection", "Keep-Alive");
                    put("Productid", "0");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "0");
                    put("TripCountry", "CN");
                    put("didi-header-hint-content", "{\"app_timeout_ms\":20000,\"Cityid\":-1,\"lang\":\"zh-CN\",\"utc_offset\":\"480\"}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .data(new HashMap<String, String>() {{
                    put("q", request.toJSONString());
                }})
                .execute();

        System.err.println("图片验证码校验返回：" + response.body());
        //{"errno":0,"error":"成功","requestid":"1538992670887587343","time":"2018-10-08 17:57:50"}
        int errno = (int) JSONPath.read(response.body(), "$.errno");
        if (errno == 0) {
            if (loginInfo.getExtra("next").equals("sms")) {
                return ResponseBuilder.RespInit("sms");
            }

        }
        String captcha = getCaptcha(loginInfo);
        return ResponseBuilder.RespInit("captcha", (String) JSONPath.read(captcha, "captchaBase64"), JSONPath.read(response.body(), "$.error") + "");

    }

    public String smsVerify(LoginInfo loginInfo) throws Exception {
        JSONObject request = new JSONObject();
        request.put("cell", loginInfo.getExtra("account"));
        request.put("code", loginInfo.getExtra("sms"));
        request.put("appversion", "5.2.22");
        request.put("api_version", "1.0.1");
        request.put("appid", 10000);
        request.put("canonical_country_code", "CN");
        request.put("channel", "73399");
        request.put("city_id", -1);
        request.put("country_calling_code", "+86");
        request.put("country_id", 156);
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", 40.0434172675);//经度
        request.put("lng", 116.2904349353);//纬度
        request.put("map_type", "soso");
        request.put("model", "ONEPLUS A6000");
        request.put("network_type", "WIFI");
        request.put("os", "9");
        request.put("role", 1);
        request.put("scene", 1);
        request.put("suuid", loginInfo.getExtra("suuid"));

        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v5/signInByCode", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/9 didihttp OneNet/2.1.0.66 com.sdu.didi.psnger/5.2.22");
                    put("Connection", "Keep-Alive");
                    put("Productid", "0");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "0");
                    put("TripCountry", "CN");
                    put("_ddns_", "1");
                    put("didi-header-hint-content", "{\"utc_offset\":\"480\",\"lang\":\"zh-CN\",\"Cityid\":-1,\"app_timeout_ms\":10000}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .data(new HashMap<String, String>() {{
                    put("q", request.toJSONString());
                }})
                .execute();

        //{"errno":40003,"error":"验证码过期","requestid":"1538993105159975447","time":"2018-10-08 18:05:05"}
        int errno = (Integer) JSONPath.read(response.body(), "$.errno");
        if (errno != 0) {
            return ResponseBuilder.RespInit("sms", "", JSONPath.read(response.body(), "$.error") + "");
        }
        loginInfo.extras.put("uid", JSONPath.read(response.body(), "$.uid"));
        loginInfo.extras.put("ticket", JSONPath.read(response.body(), "$.ticket"));
        System.err.println("登录成功");
        return ResponseBuilder.RespSuccess(getUserInfo(loginInfo));
    }

    public String passwordVerify(LoginInfo loginInfo) throws Exception {
        JSONObject request = new JSONObject();
        request.put("cell", loginInfo.getExtra("account"));
        request.put("password", "");//
        request.put("appversion", "5.2.22");
        request.put("api_version", "1.0.1");
        request.put("appid", 10000);
        request.put("canonical_country_code", "CN");
        request.put("channel", "73399");
        request.put("city_id", -1);
        request.put("country_calling_code", "+86");
        request.put("country_id", 156);
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", 40.0434172675);//经度
        request.put("lng", 116.2904349353);//纬度
        request.put("map_type", "soso");
        request.put("model", "ONEPLUS A6000");
        request.put("network_type", "WIFI");
        request.put("os", "9");
        request.put("role", 1);
        request.put("scene", 2);
        request.put("suuid", loginInfo.getExtra("suuid"));

        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v5/signInByPassword", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/9 didihttp OneNet/2.1.0.66 com.sdu.didi.psnger/5.2.22");
                    put("Connection", "Keep-Alive");
                    put("Productid", "0");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "0");
                    put("TripCountry", "CN");
                    put("didi-header-hint-content", "{\"app_timeout_ms\":20000,\"Cityid\":-1,\"lang\":\"zh-CN\",\"utc_offset\":\"480\"}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .data(new HashMap<String, String>() {{
                    put("q", request.toJSONString());
                }})
                .execute();
        //{"errno":40004,"error":"密码错误","requestid":"1538986869701773242","time":"2018-10-08 16:21:09"}
        //{"errno":0,"error":"成功","requestid":"1538986874734006746","time":"2018-10-08 16:21:14","ticket":"2BTJVRmVJYyIHuDWSGSUgYLvlT7xxVfgXs3p8pIKNBEkjDuqw0AMAO8ytTDSrqVdq339u0M-zqfZQEIqk7sHk2pgGGZjKEmddFKEYaQJo5CmqiqMSlrzpYRrL2pldzO5w0n-_hEOJAhHsnSbm5vqEu7FhDNZXVjJjdfj_TytpH6EC2le-9Kjt1m4klhvahGhYQi33_O-598AAAD__w==","uid":281475100965521,"cell":"18701666061","country_id":156,"role":1}
        String body = response.body();
        int errno = (int) JSONPath.read(body, "$.errno");
        if (errno != 0) {
            return ResponseBuilder.RespInit("password", "", JSONPath.read(body, "$.error") + "");
        }

        loginInfo.extras.put("uid", JSONPath.read(body, "$.uid"));
        loginInfo.extras.put("ticket", JSONPath.read(body, "$.ticket"));
        System.err.println("登录成功");
        return ResponseBuilder.RespSuccess(getUserInfo(loginInfo));

    }

    public String getUserInfo(LoginInfo loginInfo) throws IOException {
        Connection.Response response = jJsoup.connect("https://common.diditaxi.com.cn/passenger/getprofile", loginInfo)
                .method(Connection.Method.GET)
                .data(new HashMap<String, String>() {{
                    put("suuid", loginInfo.getExtra("suuid"));
                    put("deviceid", loginInfo.getExtra("imei"));
                    put("client_tag", "didi");
                    put("city_id", "1");
                    put("origin_id", "");
                    put("token", loginInfo.getExtra("ticket"));
                    put("role", "1");
                    put("vcode", "347");
                    put("loc_country", "86");
                    put("imei", loginInfo.getExtra("imei"));
                    put("cid", "null");
                    put("source", "0");
                    put("datatype", "1");
                    put("channel", "15");
                }})
                .headers(new HashMap<String, String>() {{
                    put("CityId", "1");
                    put("TripCountry", "CN");
                    put("_ddns_", "1");
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/9 didihttp OneNet/2.1.0.66 com.sdu.didi.psnger/5.2.22");
                    put("Connection", "Keep-Alive");
                    put("Productid", "260");
                    put("Host", "common.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("didi-header-hint-content", "{\"utc_offset\":\"480\",\"lang\":\"zh-CN\",\"Cityid\":1,\"app_timeout_ms\":20000}");
                }})
                .execute();
        return response.body();
    }
}
