package me.kagura.crawler.controller;

import me.kagura.LoginInfo;
import me.kagura.anno.LoginInfoKey;
import me.kagura.crawler.service.AuthService;
import me.kagura.crawler.utils.DidiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/didi")
public class AuthController {

    @Autowired
    AuthService authService;

    @GetMapping(value = "/init/{traceId}")
    public String init(
            @LoginInfoKey @PathVariable String traceId,
            @RequestParam String account
    ) throws Exception {
        LoginInfo loginInfo = new LoginInfo(traceId);
        loginInfo.extras.putAll(DidiUtil.getSystemInfo());
        loginInfo.extras.put("account", account);
        return authService.init(loginInfo);
    }

    @GetMapping(value = "/refresh/captcha/{traceId}")
    public String getCaptcha(
            @LoginInfoKey @PathVariable String traceId,
            LoginInfo loginInfo
    ) throws Exception {
        return authService.getCaptcha(loginInfo);
    }

    @GetMapping(value = "/refresh/sms/{traceId}")
    public String getSms(
            @LoginInfoKey @PathVariable String traceId,
            LoginInfo loginInfo
    ) throws Exception {
        return authService.sendSMS(loginInfo);
    }

    @GetMapping(value = "/verify/sms/{traceId}")
    public String verifySms(
            @LoginInfoKey @PathVariable String traceId,
            @RequestParam("sms") String sms,
            LoginInfo loginInfo
    ) throws Exception {
        loginInfo.extras.put("sms", sms);
        return authService.smsVerify(loginInfo);
    }

    @GetMapping(value = "/verify/captcha/{traceId}")
    public String verifyCaptcha(
            @LoginInfoKey @PathVariable String traceId,
            @RequestParam("captcha") String captcha,
            LoginInfo loginInfo
    ) throws Exception {
        loginInfo.extras.put("captcha", captcha);
        return authService.captchaVerify(loginInfo);
    }

    @GetMapping(value = "/verify/password/{traceId}")
    public String verifyPassword(
            @LoginInfoKey @PathVariable String traceId,
            @RequestParam("password") String password,
            LoginInfo loginInfo
    ) throws Exception {
        loginInfo.extras.put("password", password);
        return authService.passwordVerify(loginInfo);
    }

}
