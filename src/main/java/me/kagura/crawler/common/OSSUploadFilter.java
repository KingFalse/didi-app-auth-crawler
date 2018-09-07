package me.kagura.crawler.common;

import me.kagura.FollowFilter;
import me.kagura.LoginInfo;
import org.jsoup.Connection;
import org.springframework.stereotype.Component;

/**
 * 可以在doFilter方法中对response进行记录，比如上传阿里云OSS
 */
@Component
class OSSUploadFilter implements FollowFilter {

    @Override
    public void doFilter(Connection connection, LoginInfo loginInfo) {
        if (loginInfo == null) {
            return;
        }
        System.err.println(connection.response().url());
    }
}