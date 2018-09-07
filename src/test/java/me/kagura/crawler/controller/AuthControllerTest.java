package me.kagura.crawler.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.kagura.util.CaptchaTool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    String traceId = UUID.randomUUID().toString();
    String account = "18701666061";

    @Test
    public void TestAuth() throws Exception {
        MvcResult mvcResult = mvc.perform(
                MockMvcRequestBuilders.get("/didi/init/" + traceId)
                        .param("account", account)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        System.err.println("初始化返回：" + mvcResult.getResponse().getContentAsString());

        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject jsonObject = JSON.parseObject(contentAsString);
        if (jsonObject.getString("next").equals("captcha")) {
            String captcha = CaptchaTool.show(jsonObject.getString("captchaBase64"));
            System.err.println("您输入的验证码是：" + captcha);

            mvcResult = mvc.perform(
                    MockMvcRequestBuilders.get("/didi/verify/captcha/" + traceId)
                            .param("captcha", captcha)
            )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();
            System.err.println("图片验证码校验：" + mvcResult.getResponse().getContentAsString());
            jsonObject = JSON.parseObject(mvcResult.getResponse().getContentAsString());
            if (jsonObject.getString("next").equals("sms")) {
                smsVerify();
            }

        } else if (jsonObject.getString("next").equals("sms")) {
            smsVerify();
        }
    }


    public void smsVerify() throws Exception {
        String sms = CaptchaTool.show("");
        MvcResult mvcResult = mvc.perform(
                MockMvcRequestBuilders.get("/didi/verify/sms/" + traceId)
                        .param("sms", sms)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        System.err.println("短信校验返回：" + mvcResult.getResponse().getContentAsString());
    }

}