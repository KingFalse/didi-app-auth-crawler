package me.kagura.crawler.common;

import me.kagura.InitConnection;
import org.jsoup.Connection;
import org.springframework.stereotype.Component;

@Component
class initJsoup implements InitConnection {

    @Override
    public void init(Connection connection) {
//        //统一设置Fiddler代理，以便发现问题
//        connection.proxy("127.0.0.1", 8888);
    }
}