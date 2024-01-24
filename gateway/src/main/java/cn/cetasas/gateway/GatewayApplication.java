package cn.cetasas.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

// 除去 Gateway 所有的启动类都需要加上
// @EnableRedisHttpSession(flushMode = FlushMode.IMMEDIATE)
@SpringBootApplication
public class GatewayApplication {
    private static final Logger LOG = LoggerFactory.getLogger(GatewayApplication.class);

    public static void main(String[] args) {
//        SpringApplication.run(GatewayApplication.class, args);
        SpringApplication app = new SpringApplication(GatewayApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("ヾ(◍°∇°◍)ﾉﾞ\t启动成功!!");
        LOG.info("启动地址: http://127.0.0.1:{}", env.getProperty("server.port"));
    }
}
