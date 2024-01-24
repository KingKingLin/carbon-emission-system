package cn.cetasas.user;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.session.FlushMode;
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync                          // 启动异步事务
@EnableTransactionManagement          // 启动事务
@MapperScan("cn.cetasas.user.mapper") // 扫描mybatis的mapper映射文件
//@EnableRedisHttpSession(flushMode = FlushMode.IMMEDIATE)
@SpringBootApplication
public class UserApplication {
    private final static Logger LOG = LoggerFactory.getLogger(UserApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UserApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("ヾ(◍°∇°◍)ﾉﾞ\t启动成功!!");
        LOG.info("启动地址: http://127.0.0.1:{}", env.getProperty("server.port"));
    }
}
