package cn.cetasas.db.config;

import cn.cetasas.db.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {
    @Resource
    private LogInterceptor logInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                // 只有登录的用户才可以下载相关数据
                .addPathPatterns( // 黑名单
                        "/db/ces/download",
                        "/db/ees/download",
                        "/db/total/download"
                );
//                .excludePathPatterns(); // 白名单
    }
}
