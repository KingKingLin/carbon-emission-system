package cn.cetasas.user.config;

import cn.cetasas.user.interceptor.LogInterceptor;
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
                .addPathPatterns(
                        "/user/logout",
                        "/user/info/**"
                ); // 黑名单
//                .addPathPatterns("/**") // 黑名单
//                .excludePathPatterns()   // 白名单
    }
}
