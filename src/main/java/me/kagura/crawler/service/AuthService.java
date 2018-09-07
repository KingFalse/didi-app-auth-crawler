package me.kagura.crawler.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import me.kagura.FollowProcess;
import me.kagura.JJsoup;
import me.kagura.LoginInfo;
import me.kagura.crawler.utils.RSACryptography;
import me.kagura.crawler.utils.ResponseBuilder;
import me.kagura.crawler.utils.SignatureHelper;
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
        request.put("smstype", 0);
        request.put("appversion", "5.2.0");
        request.put("channel", "15");
        request.put("city_id", "1");
        request.put("client_tag", "didi");
        request.put("country_id", "%2B86");
        request.put("suuid", loginInfo.getExtra("suuid"));
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", "40.0434172675");//经度
        request.put("lng", "116.2904349353");//纬度
        request.put("os", "4.4.4");//系统版本
        request.put("maptype", "soso");
        request.put("model", "HM NOTE 1S");//机型
        request.put("networkType", "WIFI");
        request.put("origin_id", "1");
        request.put("loc_country", 86);
        request.put("role", 1);
        request.put("source", 0);
        request.put("datatype", 1);
        request.put("vcode", 347);//版本号
        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v2/gatekeeper", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/4.4.4 didihttp OneNet/2.1.0.51 com.sdu.didi.psnger/5.2.0");
                    put("Connection", "Keep-Alive");
                    put("Productid", "260");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "1");
                    put("TripCountry", "CN");
                    put("didi-header-hint-content", "{\"utc_offset\":\"480\",\"lang\":\"zh-CN\",\"Cityid\":1,\"app_timeout_ms\":10000}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .data(new HashMap<String, String>() {{
                    put("q", request.toJSONString());
                }})
                .execute();
        //{"errno":0,"error":"OK","errmsg":"OK","gkflag":1,"usertype":0,"payflag":3,"helpemail":""}
        //{"errno":-103,"error":"手机号错误"}
        if (0 != (int) JSONPath.read(response.body(), "errno")) {
            return ResponseBuilder.RespInit("init", null, JSONPath.read(response.body(), "error") + "");
        }
        String result = "";
        int gkflag = (int) JSONPath.read(response.body(), "gkflag");
        switch (gkflag) {
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
        request.put("smstype", 0);
        request.put("appversion", "5.2.0");
        request.put("channel", "15");
        request.put("city_id", "1");
        request.put("client_tag", "didi");
        request.put("country_id", "%2B86");
        request.put("suuid", loginInfo.getExtra("suuid"));
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", "40.0434172675");
        request.put("lng", "116.2904349353");
        request.put("os", "4.4.4");
        request.put("maptype", "soso");
        request.put("model", "HM NOTE 1S");
        request.put("networkType", "WIFI");
        request.put("origin_id", "1");
        request.put("loc_country", 86);
        request.put("role", 1);
        request.put("source", 0);
        request.put("datatype", 1);
        request.put("vcode", 347);
        String q = request.toJSONString();

        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v2/smsMt", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/4.4.4 didihttp OneNet/2.1.0.51 com.sdu.didi.psnger/5.2.0");
                    put("Connection", "Keep-Alive");
                    put("Productid", "260");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "1");
                    put("TripCountry", "CN");
                    put("didi-header-hint-content", "{\"utc_offset\":\"480\",\"lang\":\"zh-CN\",\"Cityid\":1,\"app_timeout_ms\":10000}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .data(new HashMap<String, String>() {{
                    put("sig", "\"" + SignatureHelper.getParamSig(q) + "\"");
                    put("q", q);
                }})
                .execute();

        int errno = (int) JSONPath.read(response.body(), "errno");
        switch (errno) {
            case 0://发送成功
                return ResponseBuilder.RespInit("sms");
            case 1003://需要图片
                loginInfo.extras.put("next", "sms");
                return getCaptcha(loginInfo);
        }
        return ResponseBuilder.RespInit("init", "", JSONPath.read(response.body(), "errmsg") + "");
    }

    public String getCaptcha(LoginInfo loginInfo) throws Exception {
        FollowProcess followProcessBase64Image = FollowProcess.FollowProcessBase64Image();
        Connection.Response response = jJsoup.connect("https://pic.risk.xiaojukeji.com/risk-pic/verification-code/img/create-get.htm", loginInfo, followProcessBase64Image)
                .method(Connection.Method.GET)
                .data(new HashMap<String, String>() {{
                    put("loc_country", "86");
                    put("os", "4.4.4");
                    put("networkType", "WIFI");
                    put("model", "HM NOTE 1S");
                    put("biz_type", "v5Login");
                    put("imei", loginInfo.getExtra("imei"));
                    put("country_id", "%2B86");
                    put("appversion", "5.2.0");
                    put("appid", "null");
                    put("maptype", "soso");
                    put("lng", "116.2904349353");
                    put("lang", "zh-CN");
                    put("suuid", loginInfo.getExtra("suuid"));
                    put("cid", "null");
                    put("client_tag", "didi");
                    put("city_id", "1");
                    put("area", "null");
                    put("source", "0");
                    put("origin_id", "1");
                    put("role", "1");
                    put("datatype", "1");
                    put("channel", "15");
                    put("lat", "40.0434172675");
                    put("key", loginInfo.getExtra("account"));
                    put("vcode", "347");
                }})
                .headers(new HashMap<String, String>() {{
                    put("Cityid", "1");
                    put("TripCountry", "CN");
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/4.4.4 didihttp OneNet/2.1.0.51 com.sdu.didi.psnger/5.2.0");
                    put("Connection", "Keep-Alive");
                    put("Productid", "260");
                    put("Host", "pic.risk.xiaojukeji.com");
                    put("Accept-Encoding", "gzip");
                    put("didi-header-hint-content", "{\"utc_offset\":\"480\",\"lang\":\"zh-CN\",\"Cityid\":1,\"app_timeout_ms\":20000}");
                }})
                .execute();

        String base64Img = followProcessBase64Image.result;
        if (base64Img.length() > 23) {
            return ResponseBuilder.RespInit("captcha", base64Img);
        }
        return ResponseBuilder.RespInit("init", null, "刷新过于频繁！请稍后重新授权！");
    }

    public String captchaVerify(LoginInfo loginInfo) throws Exception {
        JSONObject request = new JSONObject();
        request.put("cell", loginInfo.getExtra("account"));
        request.put("sendsms", "true");
        request.put("verifycode", loginInfo.getExtra("captcha"));
        request.put("verifytype", 0);
        request.put("appversion", "5.2.0");
        request.put("channel", "15");
        request.put("city_id", "1");
        request.put("client_tag", "didi");
        request.put("country_id", "+86");
        request.put("suuid", loginInfo.getExtra("suuid"));
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", "40.0434172675");
        request.put("lng", "116.2904349353");
        request.put("os", "4.4.4");
        request.put("maptype", "soso");
        request.put("model", "HM NOTE 1S");
        request.put("networkType", "WIFI");
        request.put("origin_id", "1");
        request.put("loc_country", 86);
        request.put("role", 1);
        request.put("source", 0);
        request.put("datatype", 1);
        request.put("vcode", 347);
        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v2/captchaverify", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/4.4.4 didihttp OneNet/2.1.0.51 com.sdu.didi.psnger/5.2.0");
                    put("Connection", "Keep-Alive");
                    put("Productid", "260");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "1");
                    put("TripCountry", "CN");
                    put("didi-header-hint-content", "{\"utc_offset\":\"480\",\"lang\":\"zh-CN\",\"Cityid\":1,\"app_timeout_ms\":10000}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .data(new HashMap<String, String>() {{
                    put("q", request.toJSONString());
                }})
                .execute();

        //{"errmsg":"图形验证失败","errno":2002,"error":"图形验证失败"}
        //{"email":"","errmsg":"OK","errno":0,"error":"OK"}
        int errno = (int) JSONPath.read(response.body(), "errno");
        if (errno == 0) {
            if (loginInfo.getExtra("next").equals("sms")) {
                return ResponseBuilder.RespInit("sms");
            }

        }

        String captcha = getCaptcha(loginInfo);

        return ResponseBuilder.RespInit("captcha", (String) JSONPath.read(captcha, "captchaBase64"), JSONPath.read(response.body(), "errmsg") + "");

    }

    public String smsVerify(LoginInfo loginInfo) throws Exception {
        JSONObject request = new JSONObject();
        request.put("cell", loginInfo.getExtra("account"));
        request.put("clogin", "ok");
        request.put("code", loginInfo.getExtra("sms"));
        request.put("appversion", "5.2.0");
        request.put("channel", "15");
        request.put("city_id", "-1");
        request.put("client_tag", "didi");
        request.put("country_id", "+86");
        request.put("suuid", loginInfo.getExtra("suuid"));
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", "40.0434172675");
        request.put("lng", "116.2904349353");
        request.put("os", "4.4.4");
        request.put("maptype", "soso");
        request.put("model", "HM NOTE 1S");
        request.put("networkType", "WIFI");
        request.put("origin_id", "1");
        request.put("loc_country", 86);
        request.put("role", 1);
        request.put("source", 0);
        request.put("datatype", 1);
        request.put("vcode", 347);

        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v2/sms", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/4.4.4 didihttp OneNet/2.1.0.51 com.sdu.didi.psnger/5.2.0");
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

        //{"cell":"18330225750","errmsg":"OK","errno":0,"error":"OK","pop":0,"push_adduser":1,"role":1,"skip":0,"ticket":"ZGYUe9G_ICi-en-bTzOHhskXkeTea61oqdE94wmBK-NMyDkOwlAMQMGroFe78IL5jm_DEpYCIRFRRbk7oqOcWTnSIJxoL9uPNI1pqoohXOghzPS6Y3l93ueZVmF5LrSlZ6WHHjbh-m_hRmMVoe45UhHuv0mEB23bNwAA__8=","uid":"281475103998837"}
        int errno = (Integer) JSONPath.read(response.body(), "errno");
        if (errno != 0) {
            return ResponseBuilder.RespInit("sms", "", JSONPath.read(response.body(), "errmsg") + "");
        }
        loginInfo.extras.put("uid", JSONPath.read(response.body(), "uid"));
        loginInfo.extras.put("ticket", JSONPath.read(response.body(), "ticket"));
        System.err.println("登录成功");
        return ResponseBuilder.RespSuccess(getUserInfo(loginInfo));
    }

    public String passwordVerify(LoginInfo loginInfo) throws Exception {
        JSONObject request = new JSONObject();
        request.put("cell", loginInfo.getExtra("account"));
        request.put("clogin", "ok");
        request.put("password", RSACryptography.encrypt(loginInfo.getExtra("pubkey"), loginInfo.getExtra("password")));
        request.put("rsakey", loginInfo.getExtra("rsakey"));
        request.put("appversion", "5.2.0");
        request.put("channel", "15");
        request.put("city_id", "1");
        request.put("client_tag", "didi");
        request.put("country_id", "+86");
        request.put("suuid", loginInfo.getExtra("suuid"));
        request.put("imei", loginInfo.getExtra("imei"));
        request.put("lang", "zh-CN");
        request.put("lat", "39.9346747924005");
        request.put("lng", "116.46690492548709");
        request.put("os", "4.4.4");
        request.put("maptype", "soso");
        request.put("model", "HM NOTE 1S");
        request.put("networkType", "WIFI");
        request.put("origin_id", "1");
        request.put("loc_country", 86);
        request.put("role", 1);
        request.put("source", 0);
        request.put("datatype", 1);
        request.put("vcode", 347);

        Connection.Response response = jJsoup.connect("https://epassport.diditaxi.com.cn/passport/login/v2/password", loginInfo)
                .method(Connection.Method.POST)
                .headers(new HashMap<String, String>() {{
                    put("didi-header-rid", loginInfo.getExtra("rid"));
                    put("didi-header-omgid", loginInfo.getExtra("omgid"));
                    put("User-Agent", "Android/4.4.4 didihttp OneNet/2.1.0.51 com.sdu.didi.psnger/5.2.0");
                    put("Connection", "Keep-Alive");
                    put("Productid", "260");
                    put("Host", "epassport.diditaxi.com.cn");
                    put("Accept-Encoding", "gzip");
                    put("Cityid", "1");
                    put("TripCountry", "CN");
                    put("didi-header-hint-content", "{\"utc_offset\":\"480\",\"lang\":\"zh-CN\",\"Cityid\":1,\"app_timeout_ms\":10000}");
                    put("Content-Type", "application/x-www-form-urlencoded");
                }})
                .data(new HashMap<String, String>() {{
                    put("q", request.toJSONString());
                }})
                .execute();


        String body = response.body();
        String pubkey = String.valueOf(JSONPath.read(body, "pubkey"));
        String rsakey = String.valueOf(JSONPath.read(body, "rsakey"));
        loginInfo.extras.put("pubkey", pubkey);
        loginInfo.extras.put("rsakey", rsakey);
        body = response.body();
        int errno = (int) JSONPath.read(body, "errno");
        if (errno != 0) {
            return ResponseBuilder.RespInit("password", "", JSONPath.read(body, "errmsg") + "");
        }

        loginInfo.extras.put("uid", JSONPath.read(body, "uid"));
        loginInfo.extras.put("ticket", JSONPath.read(body, "ticket"));
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
                    put("User-Agent", "Android/4.4.4 didihttp OneNet/2.1.0.51 com.sdu.didi.psnger/5.2.0");
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
