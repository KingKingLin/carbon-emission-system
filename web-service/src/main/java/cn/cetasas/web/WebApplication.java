package cn.cetasas.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
//import org.springframework.session.FlushMode;
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

//@EnableRedisHttpSession(flushMode = FlushMode.IMMEDIATE)
@SpringBootApplication
public class WebApplication {
    private final static Logger LOG = LoggerFactory.getLogger(WebApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WebApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("ヾ(◍°∇°◍)ﾉﾞ\t启动成功!!");
        LOG.info("启动地址: http://127.0.0.1:{}", env.getProperty("server.port"));
    }
}
