package me.kagura.crawler.controller;

import com.alibaba.fastjson.JSONObject;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    public void jsonPost() throws Exception {
        String jsonString = new JSONObject() {{
            put("name", "kagura");
            put("books", new JSONObject() {{
                put("count", 10);
            }});
        }}.toJSONString();
        MvcResult mvcResult = mvc.perform(
                MockMvcRequestBuilders.post("/post/json")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .content(jsonString)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        System.err.println("response：" + mvcResult.getResponse().getContentAsString());
    }

}