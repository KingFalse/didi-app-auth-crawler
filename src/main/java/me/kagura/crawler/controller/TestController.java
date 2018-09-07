package me.kagura.crawler.controller;

import me.kagura.anno.JSONBodyField;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 此类用于演示@JSONBodyField
 */
@RestController
public class TestController {

    @PostMapping(value = "/post/json")
    public String jsonPost(
            @JSONBodyField String name,
//          @JSONBodyField("$.name") String name,//简写成如上
            @JSONBodyField("$.books.count") Integer booksConut
    ) throws Exception {
        System.err.println(name);
        System.err.println(booksConut);
        return "OK";
    }

}
